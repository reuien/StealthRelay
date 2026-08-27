package com.example.gateway.controller;

import com.example.gateway.session.SessionManager;
import com.example.gateway.session.UserSession;
import com.example.gateway.support.ApiResponse;
import com.example.gateway.support.GatewayException;
import sqlConnect.FrontEndSQL;
import statistics.StatisticInfo;
import streamHandling.Digest;
import streamHandling.Token;
import usrs.DataConsumer;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api")
public class QueryController {

    private static final int MAX_POINTS = 60;
    private static final String[] BAR_CATEGORIES =
            {"0-60", "60-70", "70-80", "80-90", "90-100", "100-120"};

    private final SessionManager sessionManager;
    private final FrontEndSQL sql = new FrontEndSQL();

    public QueryController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @PostMapping("/query")
    public ApiResponse<Map<String, Object>> query(@RequestHeader("X-Token") String token,
                                                   @RequestBody Map<String, Object> body) {
        UserSession session = sessionManager.requireConsumer(token);
        String ownerName = str(body.get("ownerName"));
        long policyId = lng(body.get("policyId"));
        long streamId = lng(body.get("streamId"));
        long startMs = lng(body.get("startTime"));
        long endMs = lng(body.get("endTime"));
        long multiple = lng(body.get("multiple"));
        if (multiple <= 0) multiple = 1;

        if (ownerName.isEmpty() || policyId == 0 || streamId == 0) {
            throw new GatewayException("查询参数不完整（owner/policy/stream）");
        }
        if (startMs <= 0 || endMs <= 0 || startMs >= endMs) {
            throw new GatewayException("请提供正确的查询时间范围（起 < 止）");
        }
        Item.PrivacyPolicy policy = requireAccessiblePolicy(session, policyId);
        if (!ownerName.equals(policy.getUsrName()) || streamId != policy.getStreamID()) {
            throw new GatewayException("查询参数与当前消费者授权策略不匹配");
        }
        if (startMs < policy.getStartTime().getTime() || endMs > policy.getEndTime().getTime()) {
            throw new GatewayException("查询时间超出授权策略范围");
        }
        if (multiple < policy.getMinGranularity()) {
            throw new GatewayException("查询粒度不能小于策略授权下限：" + policy.getMinGranularity());
        }

        streamHandling.Stream curStream = sql.getStream(streamId);
        if (curStream == null) {
            throw new GatewayException("未找到对应的数据流");
        }
        long requestedEndMs = endMs;
        UploadWindow uploadWindow = readUploadWindow(ownerName, curStream);
        if (uploadWindow.uploaded && uploadWindow.endMs > 0) {
            if (startMs > uploadWindow.endMs) {
                throw new GatewayException("查询时间超出已上传 CSV 数据范围，请选择更靠前的时间");
            }
            endMs = Math.min(endMs, uploadWindow.endMs);
            if (startMs >= endMs) {
                throw new GatewayException("查询时间范围内没有已上传 CSV 数据");
            }
        }

        Map<String, Object> csvResult = queryUploadedCsv(ownerName, curStream, startMs, endMs, requestedEndMs, multiple);
        if (csvResult != null) {
            return ApiResponse.ok(csvResult);
        }

        DataConsumer dc = sessionManager.consumer(session);
        String consumerName = session.getUsrName();
        Date startDate = new Date(startMs);
        Date endDate = new Date(endMs);

        List<Digest> digests;
        Digest allDigest;
        try {
            Token tk = dc.sendRequest(consumerName, ownerName, policyId, streamId, startDate, endDate, multiple);
            if (tk == null) {
                throw new GatewayException("控制器未颁发查询令牌：请确认策略粒度传的是倍数、时间范围在授权区间内，且该流已完成真实上传");
            }
            digests = dc.getNewDigestsDC(tk, startDate, endDate, (int) tk.getGranularity());
            allDigest = dc.getAllNewDigestsDC(tk, startDate, endDate);
        } catch (Exception e) {
            throw new GatewayException("查询失败（请确认时间/粒度在策略范围内，且后端 1101/1102 已启动）：" + e.getMessage(), e);
        }

        if (digests == null || digests.isEmpty() || allDigest == null) {
            throw new GatewayException("查询无结果（时间范围或粒度可能不满足策略约束）");
        }

        int dataSize = digests.size();
        int numPoints = Math.min(dataSize, MAX_POINTS);
        int iter = Math.max(1, dataSize / numPoints);
        List<Long> time = new ArrayList<>();
        List<Double> avg = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            int index = Math.min(i * iter, dataSize - 1);
            Digest d = digests.get(index);
            long count = d.getCount();
            double a = count == 0 ? 0 : (double) d.getSum() / count;
            avg.add(a);
            time.add(d.getStartTime(curStream));
        }

        List<Long> counts = new ArrayList<>();
        counts.add(allDigest.getCount1());
        counts.add(allDigest.getCount2());
        counts.add(allDigest.getCount3());
        counts.add(allDigest.getCount4());
        counts.add(allDigest.getCount5());
        counts.add(allDigest.getCount6());

        StatisticInfo sta = StatisticInfo.getStatisticInfo(curStream, allDigest);
        double globalAvg = sta.getAverage() == null ? 0 : sta.getAverage();

        Map<String, Object> lineChart = new HashMap<>();
        lineChart.put("time", time);
        lineChart.put("avg", avg);
        lineChart.put("globalAvg", globalAvg);

        Map<String, Object> barChart = new HashMap<>();
        barChart.put("categories", BAR_CATEGORIES);
        barChart.put("counts", counts);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("mean", sta.getAverage());
        statistics.put("std", sta.getStd());
        statistics.put("variance", sta.getVariance());
        statistics.put("sum", allDigest.getSum());
        statistics.put("count", allDigest.getCount());
        statistics.put("squareSum", allDigest.getSquare());

        Map<String, Object> data = new HashMap<>();
        data.put("lineChart", lineChart);
        data.put("barChart", barChart);
        data.put("statistics", statistics);
        data.put("effectiveStartTime", startMs);
        data.put("effectiveEndTime", endMs);
        data.put("requestedEndTime", requestedEndMs);
        data.put("clippedToUploadedData", endMs < requestedEndMs);
        return ApiResponse.ok(data);
    }

    private Map<String, Object> queryUploadedCsv(String ownerName, streamHandling.Stream stream,
                                                 long startMs, long endMs, long requestedEndMs, long multiple) {
        Path csvPath = projectPath("data/uploads")
                .resolve(safePathPart(ownerName))
                .resolve(stream.getId() + ".csv");
        if (!Files.isRegularFile(csvPath)) {
            return null;
        }

        long streamStart = stream.getStartDate().getTime();
        long stepMillis = Math.max(1L, stream.getChunkSize());
        long fromIndex = Math.max(0L, (startMs - streamStart) / stepMillis);
        long toIndex = Math.max(fromIndex, (endMs - streamStart) / stepMillis);

        List<Long> values = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return null;
            }
            int valueColumn = findCsvValueColumn(header);
            String line;
            long rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                if (rowIndex > toIndex) {
                    break;
                }
                if (rowIndex >= fromIndex) {
                    Long value = parseCsvValue(line, valueColumn);
                    if (value != null) {
                        values.add(value);
                    }
                }
                rowIndex++;
            }
        } catch (IOException e) {
            return null;
        }

        if (values.isEmpty()) {
            return null;
        }

        long sum = 0L;
        long squareSum = 0L;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (long value : values) {
            sum += value;
            squareSum += value * value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double mean = (double) sum / values.size();
        double variance = 0D;
        for (long value : values) {
            double diff = value - mean;
            variance += diff * diff;
        }
        variance = variance / values.size();
        double std = Math.sqrt(variance);

        long queryMultiple = Math.max(1L, multiple);
        int bucketSize = (int) Math.min(Integer.MAX_VALUE, queryMultiple);
        List<Long> bucketTimes = new ArrayList<>();
        List<Double> bucketAverages = new ArrayList<>();
        for (int from = 0; from < values.size(); from += bucketSize) {
            int to = Math.min(values.size(), from + bucketSize);
            long bucketSum = 0L;
            for (int i = from; i < to; i++) {
                bucketSum += values.get(i);
            }
            bucketTimes.add(startMs + (long) from * stepMillis);
            bucketAverages.add((double) bucketSum / (to - from));
        }

        int numPoints = Math.min(bucketAverages.size(), MAX_POINTS);
        int iter = Math.max(1, bucketAverages.size() / Math.max(1, numPoints));
        List<Long> time = new ArrayList<>();
        List<Double> avg = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            int index = Math.min(i * iter, bucketAverages.size() - 1);
            avg.add(bucketAverages.get(index));
            time.add(bucketTimes.get(index));
        }

        List<String> categories = buildDynamicCategories(min, max);
        List<Long> counts = countDynamicBuckets(values, min, max);

        Map<String, Object> lineChart = new HashMap<>();
        lineChart.put("time", time);
        lineChart.put("avg", avg);
        lineChart.put("globalAvg", mean);
        lineChart.put("queryMultiple", queryMultiple);
        lineChart.put("bucketSize", bucketSize);

        Map<String, Object> barChart = new HashMap<>();
        barChart.put("categories", categories);
        barChart.put("counts", counts);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("mean", mean);
        statistics.put("std", std);
        statistics.put("variance", variance);
        statistics.put("sum", sum);
        statistics.put("count", values.size());
        statistics.put("squareSum", squareSum);

        Map<String, Object> data = new HashMap<>();
        data.put("lineChart", lineChart);
        data.put("barChart", barChart);
        data.put("statistics", statistics);
        data.put("effectiveStartTime", startMs);
        data.put("effectiveEndTime", endMs);
        data.put("requestedEndTime", requestedEndMs);
        data.put("clippedToUploadedData", endMs < requestedEndMs);
        data.put("queryMultiple", queryMultiple);
        data.put("bucketCount", bucketAverages.size());
        data.put("source", "csv");
        return data;
    }

    private static int findCsvValueColumn(String header) {
        String[] parts = header.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            String name = parts[i].trim().replace("\"", "").toLowerCase();
            if ("value".equals(name) || "traffic_volume".equals(name) || "vehicle_count".equals(name)) {
                return i;
            }
        }
        return Math.max(0, parts.length - 1);
    }

    private static Long parseCsvValue(String line, int valueColumn) {
        String[] parts = line.split(",", -1);
        if (valueColumn >= parts.length) {
            return null;
        }
        try {
            return Math.round(Double.parseDouble(parts[valueColumn].trim().replace("\"", "")));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<String> buildDynamicCategories(long min, long max) {
        List<String> categories = new ArrayList<>();
        if (min == max) {
            categories.add(String.valueOf(min));
            return categories;
        }
        long step = Math.max(1L, (long) Math.ceil((max - min + 1) / 6D));
        for (int i = 0; i < 6; i++) {
            long start = min + i * step;
            long end = i == 5 ? max : Math.min(max, start + step - 1);
            categories.add(start + "-" + end);
        }
        return categories;
    }

    private static List<Long> countDynamicBuckets(List<Long> values, long min, long max) {
        List<Long> counts = new ArrayList<>();
        int bucketCount = min == max ? 1 : 6;
        for (int i = 0; i < bucketCount; i++) {
            counts.add(0L);
        }
        if (min == max) {
            counts.set(0, (long) values.size());
            return counts;
        }
        long step = Math.max(1L, (long) Math.ceil((max - min + 1) / 6D));
        for (long value : values) {
            int index = (int) Math.min(5, Math.max(0, (value - min) / step));
            counts.set(index, counts.get(index) + 1);
        }
        return counts;
    }

    @GetMapping("/shared-streams")
    public ApiResponse<List<Map<String, Object>>> sharedStreams(@RequestHeader("X-Token") String token,
                                                                @RequestParam(required = false) String consumer,
                                                                @RequestParam(required = false) String owner) {
        UserSession session = sessionManager.requireConsumer(token);
        assertSameConsumer(session, consumer);
        List<Item.PrivacyPolicy> policies = sql.searchPolicy(session.getUsrName());
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Long, Boolean> seen = new HashMap<>();
        if (policies != null) {
            for (Item.PrivacyPolicy p : policies) {
                if (owner != null && !owner.isEmpty() && !owner.equals(p.getUsrName())) {
                    continue;
                }
                long sid = p.getStreamID();
                if (seen.containsKey(sid)) continue;
                seen.put(sid, true);
                Map<String, Object> m = new HashMap<>();
                m.put("streamId", String.valueOf(sid));
                String nameId = sql.searchStream_Name_ID(sid);
                m.put("streamName", nameId == null ? null : nameId.split("\\*")[0]);
                m.put("ownerName", p.getUsrName());
                m.put("policyId", String.valueOf(p.getPrivacyPolicyId()));
                m.put("minGranularity", p.getMinGranularity());
                result.add(m);
            }
        }
        return ApiResponse.ok(result);
    }

    private Item.PrivacyPolicy requireAccessiblePolicy(UserSession session, long policyId) {
        List<Item.PrivacyPolicy> policies = sql.searchPolicy(session.getUsrName());
        if (policies != null) {
            for (Item.PrivacyPolicy policy : policies) {
                if (policy.getPrivacyPolicyId() == policyId) {
                    return policy;
                }
            }
        }
        throw new GatewayException("当前消费者无权使用该策略");
    }

    private static void assertSameConsumer(UserSession session, String consumer) {
        if (consumer != null && !consumer.trim().isEmpty() && !session.getUsrName().equals(consumer.trim())) {
            throw new GatewayException("不能查看其他消费者的数据授权");
        }
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    private static long lng(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(o.toString().trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static UploadWindow readUploadWindow(String ownerName, streamHandling.Stream stream) {
        Path metaPath = projectPath("data/uploads")
                .resolve(safePathPart(ownerName))
                .resolve(stream.getId() + ".upload.properties");
        if (!Files.isRegularFile(metaPath)) {
            return UploadWindow.empty();
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(metaPath, StandardCharsets.UTF_8)) {
            properties.load(reader);
            int validRows = intProp(properties, "validRows");
            if (validRows <= 0) {
                return UploadWindow.empty();
            }
            long stepMillis = Math.max(1L, stream.getChunkSize());
            long uploadedEndMs = stream.getStartDate().getTime() + (validRows - 1L) * stepMillis;
            return new UploadWindow(true, uploadedEndMs);
        } catch (IOException e) {
            return UploadWindow.empty();
        }
    }

    private static int intProp(Properties properties, String key) {
        try {
            return Integer.parseInt(properties.getProperty(key, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String safePathPart(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static Path projectPath(String relative) {
        Path currentDir = Paths.get("").toAbsolutePath();
        if (Files.isDirectory(currentDir.resolve(relative))) {
            return currentDir.resolve(relative);
        }
        if (Files.isDirectory(currentDir.resolve("pcsig-alfred"))) {
            return currentDir.resolve("pcsig-alfred").resolve(relative);
        }
        return currentDir.resolve(relative);
    }

    private static class UploadWindow {
        private final boolean uploaded;
        private final long endMs;

        private UploadWindow(boolean uploaded, long endMs) {
            this.uploaded = uploaded;
            this.endMs = endMs;
        }

        private static UploadWindow empty() {
            return new UploadWindow(false, 0L);
        }
    }
}
