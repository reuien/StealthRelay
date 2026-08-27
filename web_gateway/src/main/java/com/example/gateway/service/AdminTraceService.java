package com.example.gateway.service;

import com.example.gateway.support.GatewayException;
import sqlConnect.Connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@org.springframework.stereotype.Service
public class AdminTraceService {
    private static final int WINDOW = 12;
    private static final double Z_THRESHOLD = 2.5D;
    private final BlockchainAnchorService anchors;

    public AdminTraceService(BlockchainAnchorService anchors) {
        this.anchors = anchors;
    }

    public Map<String, Object> trace(long streamId) {
        Map<String, Object> meta = streamMeta(streamId);
        String ownerName = String.valueOf(meta.get("ownerName"));
        Path csv = uploadRoot().resolve(safe(ownerName)).resolve(streamId + ".csv");
        List<Point> points = readPoints(csv, ((Number) meta.get("startTime")).longValue(),
                Math.max(1L, ((Number) meta.get("minGranularity")).longValue()));
        List<Map<String, Object>> anomalies = anomalies(points);
        List<Map<String, Object>> blocks = blocks(points);

        Map<String, Object> provenance = new LinkedHashMap<>();
        provenance.put("ownerNumber", meta.get("ownerNumber"));
        provenance.put("ownerName", ownerName);
        provenance.put("producerId", meta.get("producerId"));
        provenance.put("producerName", meta.get("producerName"));
        provenance.put("csvPath", Files.isRegularFile(csv) ? csv.toString() : null);
        provenance.put("source", Files.isRegularFile(csv) ? "csv" : "secure-stream/simulated");
        provenance.put("pointCount", points.size());

        List<Map<String, Object>> process = process(meta, points, blocks, anomalies);
        long traceStart = points.isEmpty() ? ((Number) meta.get("startTime")).longValue() : points.get(0).time;
        long traceEnd = points.isEmpty() ? ((Number) meta.get("endTime")).longValue() : points.get(points.size() - 1).time;
        String traceId = anchors.recordComputation(streamId, traceStart, traceEnd, process, anomalies, blocks);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("traceId", traceId);
        result.put("stream", meta);
        result.put("provenance", provenance);
        result.put("anomalies", anomalies);
        result.put("blocks", blocks);
        result.put("process", process);
        result.put("disclosure", "密文符号与钥片抵消步骤是依据项目算法和已观测统计量重建的说明，不包含真实密钥或可逆密文。");
        return result;
    }

    private Map<String, Object> streamMeta(long streamId) {
        String sql = "SELECT s.*, os.owner_id, c.usr_name, sp.producer_id, sp.producer_name " +
                "FROM stream s LEFT JOIN owner_stream os ON os.stream_id=s.id " +
                "LEFT JOIN custom c ON c.number=os.owner_id LEFT JOIN stream_producer sp ON sp.stream_id=s.id WHERE s.id=?";
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, streamId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new GatewayException("数据流不存在");
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", String.valueOf(rs.getLong("id"))); row.put("name", rs.getString("name"));
                row.put("description", rs.getString("description")); row.put("startTime", rs.getLong("starttime"));
                row.put("endTime", rs.getLong("endtime")); row.put("minGranularity", rs.getLong("mingranularity"));
                row.put("granularity", rs.getLong("granularity")); row.put("ownerNumber", rs.getString("owner_id"));
                row.put("ownerName", rs.getString("usr_name")); row.put("producerId", rs.getObject("producer_id"));
                row.put("producerName", rs.getString("producer_name")); return row;
            }
        } catch (SQLException e) {
            throw new GatewayException("读取数据流溯源失败：" + e.getMessage(), e);
        }
    }

    private List<Point> readPoints(Path csv, long start, long step) {
        if (!Files.isRegularFile(csv)) return List.of();
        List<Point> points = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String first = reader.readLine();
            if (first == null) return points;
            String[] header = first.split(",", -1);
            boolean hasHeader = first.matches(".*[a-zA-Z\\u4e00-\\u9fa5].*");
            int timeCol = hasHeader ? findColumn(header, "time", "date", "时间", "日期") : (header.length > 1 ? 0 : -1);
            int valueCol = hasHeader ? findValueColumn(header, timeCol) : Math.max(0, header.length - 1);
            long index = 0;
            if (!hasHeader) { Point point = parsePoint(first, timeCol, valueCol, start, step, index++); if (point != null) points.add(point); }
            String line;
            while ((line = reader.readLine()) != null && points.size() < 10000) {
                Point point = parsePoint(line, timeCol, valueCol, start, step, index++);
                if (point != null) points.add(point);
            }
        } catch (IOException ignored) { }
        return points;
    }

    private Point parsePoint(String line, int timeCol, int valueCol, long start, long step, long index) {
        String[] parts = line.split(",", -1);
        if (valueCol >= parts.length) return null;
        try {
            double value = Double.parseDouble(parts[valueCol].trim().replace("\"", ""));
            long time = timeCol >= 0 && timeCol < parts.length ? parseTime(parts[timeCol].trim(), start + index * step) : start + index * step;
            return new Point(time, value, index);
        } catch (NumberFormatException e) { return null; }
    }

    private List<Map<String, Object>> anomalies(List<Point> points) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            int from = Math.max(0, i - WINDOW); int to = Math.min(points.size(), i + WINDOW + 1);
            if (to - from < 5) continue;
            double sum = 0D; for (int j = from; j < to; j++) if (j != i) sum += points.get(j).value;
            int count = to - from - 1; double mean = sum / count; double variance = 0D;
            for (int j = from; j < to; j++) if (j != i) { double d = points.get(j).value - mean; variance += d * d; }
            double std = Math.sqrt(variance / count); double z = std == 0 ? 0 : Math.abs(points.get(i).value - mean) / std;
            double change = i == 0 || points.get(i - 1).value == 0 ? 0 : Math.abs(points.get(i).value - points.get(i - 1).value) / Math.abs(points.get(i - 1).value);
            if (z >= Z_THRESHOLD || change >= .5D) {
                Map<String, Object> row = new LinkedHashMap<>(); Point p = points.get(i);
                row.put("index", p.index); row.put("time", p.time); row.put("value", p.value);
                row.put("localMean", round(mean)); row.put("localStd", round(std)); row.put("zScore", round(z));
                row.put("changeRate", round(change * 100D));
                row.put("severity", z >= 4 || change >= 1 ? "critical" : z >= 3 ? "high" : "medium");
                row.put("reason", z >= Z_THRESHOLD && change >= .5D ? "局部离群且发生突变" : z >= Z_THRESHOLD ? "偏离局部统计区间" : "相邻流量突变");
                rows.add(row);
            }
        }
        return rows;
    }

    private List<Map<String, Object>> blocks(List<Point> points) {
        List<Map<String, Object>> rows = new ArrayList<>(); int size = Math.max(1, (int) Math.ceil(points.size() / 24D));
        for (int from = 0; from < points.size(); from += size) {
            int to = Math.min(points.size(), from + size); double sum = 0, square = 0;
            for (int i = from; i < to; i++) { sum += points.get(i).value; square += points.get(i).value * points.get(i).value; }
            Map<String, Object> row = new LinkedHashMap<>(); int blockIndex = rows.size();
            row.put("blockIndex", blockIndex); row.put("startTime", points.get(from).time); row.put("endTime", points.get(to - 1).time);
            row.put("count", to - from); row.put("sum", round(sum)); row.put("squareSum", round(square));
            row.put("mean", round(sum / (to - from)));
            row.put("cipherSymbol", "C" + blockIndex + "=Enc(Σx," + shortHash(streamHash(points, from, to)) + ")");
            row.put("evidence", "derived"); rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> process(Map<String, Object> meta, List<Point> points,
                                               List<Map<String, Object>> blocks, List<Map<String, Object>> anomalies) {
        List<Map<String, Object>> steps = new ArrayList<>();
        steps.add(step(1, "身份与范围校验", "确认超级管理员角色并定位流 " + meta.get("id"), "observed"));
        steps.add(step(2, "来源解析", "关联 Owner、Producer、上传文件与 " + points.size() + " 个数据点", "observed"));
        steps.add(step(3, "时间片切分", "按最小粒度 " + meta.get("minGranularity") + " ms 切片，并形成 " + blocks.size() + " 个展示块", "observed"));
        steps.add(step(4, "密文块内聚合", "同态性质：Enc(x₁) ⊕ … ⊕ Enc(xₙ) = Enc(Σx)，服务端无需查看明文即可合并", "derived"));
        steps.add(step(5, "边界钥片抵消", "内部钥片 kᵢ−kᵢ₊₁ 望远镜式抵消，仅保留查询区间左右边界项", "derived"));
        steps.add(step(6, "统计量恢复", "由 Σx、Σx² 与 n 计算 mean=Σx/n，variance=Σx²/n−mean²", "derived"));
        steps.add(step(7, "异常定位", "滑动窗口 ±" + WINDOW + " 点，阈值 |z|≥" + Z_THRESHOLD + " 或相邻突变≥50%，命中 " + anomalies.size() + " 点", "observed"));
        return steps;
    }

    private static Map<String, Object> step(int order, String name, String detail, String evidence) {
        Map<String, Object> row = new LinkedHashMap<>(); row.put("order", order); row.put("name", name);
        row.put("detail", detail); row.put("evidence", evidence); return row;
    }

    private static int findColumn(String[] header, String... names) {
        for (int i = 0; i < header.length; i++) { String h = header[i].toLowerCase(Locale.ROOT); for (String n : names) if (h.contains(n)) return i; }
        return -1;
    }
    private static int findValueColumn(String[] header, int timeCol) {
        int preferred = findColumn(header, "vehicle_flow", "traffic", "flow", "volume", "count", "value", "流量", "车辆");
        if (preferred >= 0 && preferred != timeCol) return preferred;
        for (int i = 0; i < header.length; i++) if (i != timeCol) return i; return 0;
    }
    private static long parseTime(String raw, long fallback) {
        raw = raw.replace("\"", "").trim();
        try { long n = Long.parseLong(raw); return Math.abs(n) < 100_000_000_000L ? n * 1000L : n; } catch (NumberFormatException ignored) { }
        for (DateTimeFormatter f : List.of(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)) {
            try { return LocalDateTime.parse(raw.replace('T', ' '), f).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(); } catch (Exception ignored) { }
        }
        return fallback;
    }
    private static long streamHash(List<Point> points, int from, int to) { long h = 1125899906842597L; for (int i=from;i<to;i++) h=31*h+Double.doubleToLongBits(points.get(i).value); return h; }
    private static String shortHash(long value) { return Long.toHexString(value).substring(0, Math.min(8, Long.toHexString(value).length())); }
    private static double round(double value) { return Math.round(value * 100D) / 100D; }
    private static String safe(String value) { return value == null ? "unknown" : value.replaceAll("[^a-zA-Z0-9._-]", "_"); }
    private static Path uploadRoot() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) { if (Files.isDirectory(dir.resolve("web_gateway"))) return dir.resolve("data/uploads"); if (Files.isDirectory(dir.resolve("pcsig-alfred/web_gateway"))) return dir.resolve("pcsig-alfred/data/uploads"); dir=dir.getParent(); }
        return Paths.get("data/uploads");
    }
    private record Point(long time, double value, long index) { }
}
