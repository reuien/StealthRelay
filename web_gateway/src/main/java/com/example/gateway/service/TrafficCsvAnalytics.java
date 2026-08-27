package com.example.gateway.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficCsvAnalytics {
    private static final int DEFAULT_MAX_BLOCKS = 80;

    public Map<String, Object> queryTraffic(String ownerName, streamHandling.Stream stream,
                                            long startMs, long endMs, long blockMillis) {
        List<Long> values = readValues(ownerName, stream, startMs, endMs);
        if (values.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("blocks", List.of());
            empty.put("statistics", statistics(List.of()));
            empty.put("lineChart", lineChart(List.of(), startMs, Math.max(1L, blockMillis)));
            empty.put("barChart", barChart(List.of()));
            return empty;
        }
        long stepMillis = Math.max(1L, stream.getChunkSize());
        int bucketSize = (int) Math.max(1L, blockMillis / stepMillis);
        List<Map<String, Object>> blocks = buildBlocks(values, startMs, stepMillis, bucketSize, DEFAULT_MAX_BLOCKS);
        Map<String, Object> result = new HashMap<>();
        result.put("blocks", blocks);
        result.put("statistics", statistics(values));
        result.put("lineChart", lineChart(values, startMs, stepMillis));
        result.put("barChart", barChart(values));
        result.put("source", "csv");
        return result;
    }

    public Map<String, Object> federation(List<FederationStream> streams, long startMs, long endMs) {
        List<Long> allValues = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int index = 1;
        for (FederationStream item : streams) {
            List<Long> values = readValues(item.ownerName, item.stream, startMs, endMs);
            allValues.addAll(values);
            Map<String, Object> row = new HashMap<>();
            row.put("index", index++);
            row.put("ownerName", item.ownerName);
            row.put("streamId", String.valueOf(item.stream.getId()));
            row.put("streamName", item.stream.getName());
            row.put("policyId", String.valueOf(item.policyId));
            row.put("startTime", startMs);
            row.put("endTime", endMs);
            row.put("count", values.size());
            row.put("average", values.isEmpty() ? 0D : statistics(values).get("mean"));
            rows.add(row);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows", rows);
        result.put("statistics", statistics(allValues));
        result.put("source", "csv-federation");
        return result;
    }

    public List<Long> readValues(String ownerName, streamHandling.Stream stream, long startMs, long endMs) {
        Path csvPath = projectPath("data/uploads")
                .resolve(safePathPart(ownerName))
                .resolve(stream.getId() + ".csv");
        if (!Files.isRegularFile(csvPath)) {
            return List.of();
        }
        long streamStart = stream.getStartDate().getTime();
        long stepMillis = Math.max(1L, stream.getChunkSize());
        long fromIndex = Math.max(0L, (startMs - streamStart) / stepMillis);
        long toIndex = Math.max(fromIndex, (endMs - streamStart) / stepMillis);
        List<Long> values = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return List.of();
            }
            int valueColumn = findValueColumn(header);
            String line;
            long rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                if (rowIndex > toIndex) {
                    break;
                }
                if (rowIndex >= fromIndex) {
                    Long value = parseValue(line, valueColumn);
                    if (value != null) {
                        values.add(value);
                    }
                }
                rowIndex++;
            }
        } catch (IOException e) {
            return List.of();
        }
        return values;
    }

    private static List<Map<String, Object>> buildBlocks(List<Long> values, long startMs, long stepMillis,
                                                         int bucketSize, int maxBlocks) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        int totalBuckets = (int) Math.ceil(values.size() / (double) bucketSize);
        int stride = Math.max(1, (int) Math.ceil(totalBuckets / (double) maxBlocks));
        for (int bucket = 0; bucket < totalBuckets; bucket += stride) {
            int from = bucket * bucketSize;
            int to = Math.min(values.size(), from + bucketSize * stride);
            List<Long> slice = values.subList(from, to);
            Map<String, Object> stat = statistics(slice);
            long blockStart = startMs + (long) from * stepMillis;
            long blockEnd = startMs + (long) Math.max(from, to - 1) * stepMillis;
            double mean = number(stat.get("mean"));
            double congestion = Math.min(98D, Math.max(5D, mean / 90D));
            Map<String, Object> block = new HashMap<>();
            block.put("range", "数据块 " + from + "—" + Math.max(from, to - 1));
            block.put("startTime", blockStart);
            block.put("endTime", blockEnd);
            block.put("route", "ROAD-A" + ((blocks.size() % 3) + 1));
            block.put("vehicleFlow", mean);
            block.put("avgSpeed", Math.max(12D, 68D - congestion * 0.45D));
            block.put("occupancy", congestion);
            block.put("weightedIndex", mean * (1D + congestion / 100D));
            block.put("count", stat.get("count"));
            block.put("average", stat.get("mean"));
            block.put("integral", stat.get("sum"));
            blocks.add(block);
        }
        return blocks;
    }

    private static Map<String, Object> statistics(List<Long> values) {
        Map<String, Object> statistics = new HashMap<>();
        if (values.isEmpty()) {
            statistics.put("mean", 0D);
            statistics.put("std", 0D);
            statistics.put("variance", 0D);
            statistics.put("sum", 0L);
            statistics.put("count", 0);
            statistics.put("squareSum", 0L);
            statistics.put("min", 0L);
            statistics.put("max", 0L);
            return statistics;
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
        statistics.put("mean", mean);
        statistics.put("std", Math.sqrt(variance));
        statistics.put("variance", variance);
        statistics.put("sum", sum);
        statistics.put("count", values.size());
        statistics.put("squareSum", squareSum);
        statistics.put("min", min);
        statistics.put("max", max);
        return statistics;
    }

    private static Map<String, Object> lineChart(List<Long> values, long startMs, long stepMillis) {
        int maxPoints = 80;
        int numPoints = Math.min(values.size(), maxPoints);
        int stride = Math.max(1, values.isEmpty() ? 1 : values.size() / Math.max(1, numPoints));
        List<Long> time = new ArrayList<>();
        List<Double> avg = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            int index = Math.min(i * stride, values.size() - 1);
            time.add(startMs + (long) index * stepMillis);
            avg.add(values.get(index).doubleValue());
        }
        Map<String, Object> chart = new HashMap<>();
        chart.put("time", time);
        chart.put("avg", avg);
        chart.put("globalAvg", number(statistics(values).get("mean")));
        return chart;
    }

    private static Map<String, Object> barChart(List<Long> values) {
        Map<String, Object> stat = statistics(values);
        long min = ((Number) stat.get("min")).longValue();
        long max = ((Number) stat.get("max")).longValue();
        List<String> categories = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        if (values.isEmpty() || min == max) {
            categories.add(values.isEmpty() ? "0" : String.valueOf(min));
            counts.add((long) values.size());
        } else {
            long step = Math.max(1L, (long) Math.ceil((max - min + 1) / 6D));
            for (int i = 0; i < 6; i++) {
                long start = min + i * step;
                long end = i == 5 ? max : Math.min(max, start + step - 1);
                categories.add(start + "-" + end);
                counts.add(0L);
            }
            for (long value : values) {
                int index = (int) Math.min(5, Math.max(0, (value - min) / step));
                counts.set(index, counts.get(index) + 1);
            }
        }
        Map<String, Object> chart = new HashMap<>();
        chart.put("categories", categories);
        chart.put("counts", counts);
        return chart;
    }

    private static int findValueColumn(String header) {
        String[] parts = header.split(",", -1);
        String[] preferredNames = {
                "vehicle_flow", "traffic_flow", "traffic_volume", "vehicle_count",
                "vehiclecount", "car_count", "flow", "volume", "count",
                "车流量", "交通流量", "车辆数", "车流", "流量"
        };
        for (String preferredName : preferredNames) {
            for (int i = 0; i < parts.length; i++) {
                String name = normalizeHeader(parts[i]);
                if (name.equals(preferredName) || name.contains(preferredName)) {
                    return i;
                }
            }
        }
        for (int i = 0; i < parts.length; i++) {
            String name = normalizeHeader(parts[i]);
            if ("value".equals(name) || name.contains("value") || name.contains("metric")
                    || name.contains("amount") || name.contains("price") || name.contains("数值")
                    || name.contains("值")) {
                return i;
            }
        }
        return Math.max(0, parts.length - 1);
    }

    private static String normalizeHeader(String header) {
        return header == null ? "" : header.trim().replace("\"", "").toLowerCase();
    }

    private static Long parseValue(String line, int valueColumn) {
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

    private static double number(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0D;
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

    public static class FederationStream {
        public final String ownerName;
        public final long policyId;
        public final streamHandling.Stream stream;

        public FederationStream(String ownerName, long policyId, streamHandling.Stream stream) {
            this.ownerName = ownerName;
            this.policyId = policyId;
            this.stream = stream;
        }
    }
}
