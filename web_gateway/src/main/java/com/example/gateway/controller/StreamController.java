package com.example.gateway.controller;

import com.example.gateway.session.SessionManager;
import com.example.gateway.session.UserSession;
import com.example.gateway.support.ApiResponse;
import com.example.gateway.support.GatewayException;
import com.example.gateway.support.PrecisionUtil;
import com.example.gateway.service.StreamUploadFingerprintService;
import sqlConnect.FrontEndSQL;
import Item.Equipment;
import Item.Stream;
import streamHandling.DataPoint;
import streamHandling.TimeUtil;
import usrs.DataOwnerClient;
import sqlConnect.Connect;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.HexFormat;

@RestController
@RequestMapping("/api")
public class StreamController {

    private static final Path CSV_UPLOAD_ROOT = projectPath("data/uploads");
    private static final int MAX_CSV_UPLOAD_POINTS = 3000;

    private final SessionManager sessionManager;
    private final StreamUploadFingerprintService fingerprints;
    private final FrontEndSQL sql = new FrontEndSQL();

    public StreamController(SessionManager sessionManager, StreamUploadFingerprintService fingerprints) {
        this.sessionManager = sessionManager;
        this.fingerprints = fingerprints;
    }

    @GetMapping("/streams")
    public ApiResponse<List<Map<String, Object>>> list(@RequestHeader("X-Token") String token,
                                                       @RequestParam(required = false) String ownerId) {
        UserSession session = sessionManager.requireOwner(token);
        assertSameOwner(session, ownerId);
        ownerId = session.getNumber();
        List<Stream> streams = sql.searchStream(ownerId);
        List<Item.PrivacyPolicy> ownerPolicies = sql.searchPolicyByOwner(session.getUsrName());
        List<Map<String, Object>> result = new ArrayList<>();
        if (streams != null) {
            for (Stream s : streams) {
                UploadStatus uploadStatus = readUploadStatus(session, s.getId());
                int authorizationCount = countPolicies(ownerPolicies, s.getId());
                Map<String, Object> m = new HashMap<>();
                m.put("id", String.valueOf(s.getId()));
                m.put("name", s.getName());
                m.put("description", s.getDesciption());
                m.put("startTime", s.getStarttime() == null ? null : s.getStarttime().getTime());
                m.put("endTime", s.getEndtime() == null ? null : s.getEndtime().getTime());
                m.put("minGranularity", s.getMingranularity());
                m.put("granularity", s.getGranularity());
                m.put("uploaded", uploadStatus.uploaded);
                m.put("uploadSource", uploadStatus.source);
                m.put("csvTotalRows", uploadStatus.totalRows);
                m.put("csvValidRows", uploadStatus.validRows);
                m.put("csvIgnoredRows", uploadStatus.ignoredRows);
                m.put("csvTimeMode", uploadStatus.timeMode);
                m.put("csvSampled", uploadStatus.sampled);
                m.put("uploadedAt", uploadStatus.uploadedAt);
                m.put("authorizationCount", authorizationCount);
                m.put("authorized", authorizationCount > 0);
                m.put("producerBound", sql.getStreamProducer(session.getNumber(), s.getId()) != null);
                m.put("readyForQuery", uploadStatus.uploaded && authorizationCount > 0);
                result.add(m);
            }
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/streams")
    public ApiResponse<Map<String, Object>> create(@RequestHeader("X-Token") String token,
                                                    @RequestBody Map<String, Object> body) {
        UserSession session = sessionManager.requireOwner(token);
        String name = str(body.get("name"));
        String description = str(body.get("description"));
        long startMs = lng(body.get("startTime"));
        long endMs = lng(body.get("endTime"));
        long minGranMs = lng(body.get("minGranularityMillis"));
        long granMs = lng(body.get("granularityMillis"));
        long producerId = lng(body.get("producerId"));
        String producerName = str(body.get("producerName"));

        if (name.isEmpty()) {
            throw new GatewayException("流名称不能为空");
        }
        if (startMs <= 0 || endMs <= 0 || startMs >= endMs) {
            throw new GatewayException("请提供正确的起止时间（起 < 止）");
        }
        if (producerId <= 0) {
            throw new GatewayException("请选择一个生产者设备");
        }
        assertOwnedProducer(session, producerId, producerName);

        TimeUtil.Precision chunkSize = PrecisionUtil.fromMillis(minGranMs);
        List<TimeUtil.Precision> resolutionLevels = new ArrayList<>();

        DataOwnerClient doc = sessionManager.ownerClient(session);
        long streamId;
        try {
            doc.linkProducer(producerId, producerName);
            streamId = doc.createStream(name, description, new Date(startMs), new Date(endMs),
                    chunkSize, resolutionLevels);
        } catch (Exception e) {
            throw new GatewayException("创建流失败（请确认后端 1101/1234 已启动）：" + e.getMessage(), e);
        }

        Stream item = new Stream();
        item.setId(streamId);
        item.setName(name);
        item.setDesciption(description);
        item.setStarttime(new Date(startMs));
        item.setEndtime(new Date(endMs));
        item.setMingranularity(minGranMs > 0 ? minGranMs : chunkSize.getMillis());
        item.setGranularity(granMs > 0 ? granMs : minGranMs);
        sql.insertStream(item);
        sql.insertOwner_Stream(session.getNumber(), streamId);
        sql.upsertStreamProducer(session.getNumber(), streamId, producerId, producerName);

        Map<String, Object> data = new HashMap<>();
        data.put("id", String.valueOf(streamId));
        data.put("name", name);
        return ApiResponse.ok(data);
    }

    @PostMapping("/streams/{id}/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestHeader("X-Token") String token,
                                                    @PathVariable("id") String id,
                                                    @RequestParam(value = "file", required = false) MultipartFile file) {
        UserSession session = sessionManager.requireOwner(token);
        long streamId;
        try {
            streamId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new GatewayException("非法的流 ID");
        }
        assertOwnedStream(session, streamId);
        DataOwnerClient doc = sessionManager.ownerClient(session);
        CsvUploadSummary summary;
        try {
            Stream stream = findOwnedStream(session, streamId);
            summary = prepareCsvSource(session, stream, file);
            ensureLinkedProducer(session, doc, streamId);
            if ("csv".equals(summary.source)) {
                doc.uploadDataPoints(streamId, summary.dataPoints);
            } else {
                doc.producerUploadData(streamId);
            }
            writeUploadStatus(session, streamId, summary);
            if ("csv".equals(summary.source)) {
                Path csvPath = CSV_UPLOAD_ROOT.resolve(safePathPart(session.getUsrName())).resolve(streamId + ".csv");
                summary.fileSha256 = sha256(csvPath);
                summary.fileSize = Files.size(csvPath);
                summary.fileName = file == null || file.getOriginalFilename() == null
                        ? streamId + ".csv" : Paths.get(file.getOriginalFilename()).getFileName().toString();
                summary.uploadedAt = System.currentTimeMillis();
                fingerprints.recordCsv(streamId, session.getNumber(), session.getUsrName(), summary.fileName,
                        summary.fileSha256, summary.fileSize, summary.totalRows, summary.validRows, summary.uploadedAt);
            }
        } catch (Exception e) {
            throw new GatewayException("上传数据失败（请确认后端 1101/1234 与 Kafka 已就绪）：" + e.getMessage(), e);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("uploaded", true);
        data.put("source", summary.source);
        data.put("processedBy", "csv".equals(summary.source) ? "gateway" : "producer");
        data.put("validRows", summary.validRows);
        data.put("ignoredRows", summary.ignoredRows);
        data.put("totalRows", summary.totalRows);
        data.put("timeMode", summary.timeMode);
        data.put("sampled", summary.sampled);
        data.put("maxUploadPoints", MAX_CSV_UPLOAD_POINTS);
        data.put("fileSha256", summary.fileSha256);
        data.put("credentialStatus", "csv".equals(summary.source) ? "AWAITING_POLICY" : "NOT_APPLICABLE");
        return ApiResponse.ok(data);
    }

    @DeleteMapping("/streams/{id}")
    public ApiResponse<Map<String, Object>> delete(@RequestHeader("X-Token") String token,
                                                    @PathVariable("id") String id) {
        UserSession session = sessionManager.requireOwner(token);
        long streamId;
        try {
            streamId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new GatewayException("非法的流 ID");
        }
        assertOwnedStream(session, streamId);

        deleteStreamRecords(session, streamId);

        boolean filesDeleted = deleteUploadFiles(session, streamId);
        boolean dataServerDeleted = false;
        try {
            dataServerDeleted = sessionManager.ownerClient(session).deleteStream(streamId);
        } catch (Exception ignored) {
            // The Web-side deletion is already committed. Remote cleanup is best-effort so
            // a temporarily unavailable DataServer does not resurrect a deleted stream.
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("deleted", true);
        data.put("filesDeleted", filesDeleted);
        data.put("dataServerDeleted", dataServerDeleted);
        return ApiResponse.ok(data);
    }

    private void deleteStreamRecords(UserSession session, long streamId) {
        try (Connection connection = Connect.getConnection()) {
            connection.setAutoCommit(false);
            try {
                executeDelete(connection, "DELETE FROM history WHERE streamid = ?", streamId);
                executeDelete(connection, "DELETE FROM policy WHERE stream_id = ?", streamId);
                executeDelete(connection, "DELETE FROM policy_mpc WHERE stream_id = ?", streamId);
                executeDelete(connection, "DELETE FROM stream_producer WHERE stream_id = ? AND owner_id = ?",
                        streamId, session.getNumber());
                executeDelete(connection, "DELETE FROM owner_stream WHERE stream_id = ? AND owner_id = ?",
                        streamId, session.getNumber());
                int deleted = executeDelete(connection, "DELETE FROM stream WHERE id = ?", streamId);
                if (deleted != 1) {
                    throw new SQLException("数据流记录不存在或已被删除");
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new GatewayException("删除数据流失败，数据库更改已回滚：" + e.getMessage(), e);
        }
    }

    private static int executeDelete(Connection connection, String sqlText, Object... parameters)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sqlText)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            return statement.executeUpdate();
        }
    }

    private boolean deleteUploadFiles(UserSession session, long streamId) {
        Path ownerDir = CSV_UPLOAD_ROOT.resolve(safePathPart(session.getUsrName()));
        try {
            Files.deleteIfExists(ownerDir.resolve(streamId + ".csv"));
            Files.deleteIfExists(ownerDir.resolve(streamId + ".upload.properties"));
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private CsvUploadSummary prepareCsvSource(UserSession session, Stream stream, MultipartFile file) throws IOException {
        Path ownerDir = CSV_UPLOAD_ROOT.resolve(safePathPart(session.getUsrName()));
        long streamId = stream.getId();
        Path csvPath = ownerDir.resolve(streamId + ".csv");
        if (file == null || file.isEmpty()) {
            Files.deleteIfExists(csvPath);
            return new CsvUploadSummary("simulated", 0, 0, 0);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new GatewayException("只支持上传 .csv 文件");
        }
        CsvUploadSummary summary = inspectCsv(file, stream);
        if (summary.validRows == 0) {
            throw new GatewayException("CSV 没有可写入的数据：请检查文件是否有数据行，或数据流时间范围是否足够容纳 CSV 行");
        }
        Files.createDirectories(ownerDir);
        file.transferTo(csvPath.toFile());
        return summary;
    }

    private CsvUploadSummary inspectCsv(MultipartFile file, Stream stream) throws IOException {
        int totalRows = 0;
        int validRows = 0;
        int ignoredRows = 0;
        List<DataPoint> dataPoints = new ArrayList<>();
        List<ParsedCsvRow> parsedRows = new ArrayList<>();
        long streamStart = stream.getStarttime().getTime();
        long streamEnd = stream.getEndtime().getTime();
        long stepMillis = Math.max(1L, stream.getMingranularity());
        boolean headerParsed = false;
        boolean hasTimestampColumn = true;
        int timestampColumn = 0;
        int valueColumn = 1;
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            int lineNumber = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 1) {
                    throw new GatewayException("CSV 第 " + lineNumber + " 行格式错误，至少需要一列数值");
                }
                if (!headerParsed && isHeader(parts)) {
                    timestampColumn = findTimestampColumn(parts);
                    hasTimestampColumn = timestampColumn >= 0;
                    valueColumn = findValueColumn(parts, timestampColumn);
                    headerParsed = true;
                    continue;
                }
                if (!headerParsed && parts.length == 1) {
                    hasTimestampColumn = false;
                    valueColumn = 0;
                }
                headerParsed = true;
                totalRows++;
                if (hasTimestampColumn && timestampColumn >= parts.length) {
                    throw new GatewayException("CSV 第 " + lineNumber + " 行缺少时间列");
                }
                if (valueColumn >= parts.length) {
                    throw new GatewayException("CSV 第 " + lineNumber + " 行缺少可用数值列");
                }
                long timestamp = hasTimestampColumn
                        ? parseTimestamp(parts[timestampColumn].trim(), lineNumber)
                        : streamStart + parsedRows.size() * stepMillis;
                long value = parseValue(parts[valueColumn].trim(), lineNumber);
                parsedRows.add(new ParsedCsvRow(timestamp, value));
                if (timestamp < streamStart || timestamp > streamEnd) {
                    ignoredRows++;
                    continue;
                }
                dataPoints.add(new DataPoint(new Date(timestamp), value));
                validRows++;
            }
        }
        if (validRows > 0) {
            String timeMode = hasTimestampColumn ? "original" : "generated";
            if (dataPoints.size() > MAX_CSV_UPLOAD_POINTS) {
                List<DataPoint> sampledPoints = sampleCsvRows(parsedRows, streamStart, streamEnd, stepMillis, MAX_CSV_UPLOAD_POINTS);
                return new CsvUploadSummary("csv", totalRows, sampledPoints.size(),
                        Math.max(0, totalRows - sampledPoints.size()), sampledPoints, "sampled", true);
            }
            return new CsvUploadSummary("csv", totalRows, validRows, ignoredRows, dataPoints, timeMode);
        }
        for (ParsedCsvRow parsedRow : parsedRows) {
            long mappedTimestamp = streamStart + dataPoints.size() * stepMillis;
            if (mappedTimestamp > streamEnd) {
                break;
            }
            dataPoints.add(new DataPoint(new Date(mappedTimestamp), parsedRow.value));
        }
        if (dataPoints.size() > MAX_CSV_UPLOAD_POINTS) {
            dataPoints = sampleCsvRows(parsedRows, streamStart, streamEnd, stepMillis, MAX_CSV_UPLOAD_POINTS);
        }
        if (!dataPoints.isEmpty()) {
            return new CsvUploadSummary("csv", totalRows, dataPoints.size(),
                    Math.max(0, totalRows - dataPoints.size()), dataPoints,
                    dataPoints.size() < totalRows ? "sampled" : "rebased", dataPoints.size() < totalRows);
        }
        return new CsvUploadSummary("csv", totalRows, 0, totalRows, dataPoints, "none");
    }

    private List<DataPoint> sampleCsvRows(List<ParsedCsvRow> rows, long streamStart, long streamEnd,
                                          long stepMillis, int maxPoints) {
        List<DataPoint> sampledPoints = new ArrayList<>();
        if (rows.isEmpty() || maxPoints <= 0) {
            return sampledPoints;
        }
        int points = Math.min(maxPoints, rows.size());
        for (int i = 0; i < points; i++) {
            int sourceIndex = points == 1 ? 0 : (int) Math.round(i * (rows.size() - 1D) / (points - 1D));
            long mappedTimestamp = streamStart + (long) i * stepMillis;
            if (mappedTimestamp > streamEnd) {
                break;
            }
            sampledPoints.add(new DataPoint(new Date(mappedTimestamp), rows.get(sourceIndex).value));
        }
        return sampledPoints;
    }

    private UploadStatus readUploadStatus(UserSession session, long streamId) {
        Path metaPath = uploadMetaPath(session, streamId);
        if (!Files.isRegularFile(metaPath)) {
            return UploadStatus.empty();
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(metaPath, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return new UploadStatus(
                    true,
                    properties.getProperty("source", "unknown"),
                    intProp(properties, "totalRows"),
                    intProp(properties, "validRows"),
                    intProp(properties, "ignoredRows"),
                    longProp(properties, "uploadedAt"),
                    properties.getProperty("timeMode", "unknown"),
                    Boolean.parseBoolean(properties.getProperty("sampled", "false"))
            );
        } catch (IOException e) {
            return UploadStatus.empty();
        }
    }

    private void writeUploadStatus(UserSession session, long streamId, CsvUploadSummary summary) throws IOException {
        Path ownerDir = CSV_UPLOAD_ROOT.resolve(safePathPart(session.getUsrName()));
        Files.createDirectories(ownerDir);
        Properties properties = new Properties();
        properties.setProperty("source", summary.source);
        properties.setProperty("totalRows", String.valueOf(summary.totalRows));
        properties.setProperty("validRows", String.valueOf(summary.validRows));
        properties.setProperty("ignoredRows", String.valueOf(summary.ignoredRows));
        properties.setProperty("timeMode", summary.timeMode);
        properties.setProperty("sampled", String.valueOf(summary.sampled));
        properties.setProperty("uploadedAt", String.valueOf(System.currentTimeMillis()));
        try (java.io.Writer writer = Files.newBufferedWriter(uploadMetaPath(session, streamId), StandardCharsets.UTF_8)) {
            properties.store(writer, "stream upload status");
        }
    }

    private Path uploadMetaPath(UserSession session, long streamId) {
        return CSV_UPLOAD_ROOT.resolve(safePathPart(session.getUsrName())).resolve(streamId + ".upload.properties");
    }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (java.io.InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read > 0) digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前Java环境不支持SHA-256", e);
        }
    }

    private Stream findOwnedStream(UserSession session, long streamId) {
        List<Stream> streams = sql.searchStream(session.getNumber());
        if (streams != null) {
            for (Stream stream : streams) {
                if (stream.getId() == streamId) {
                    return stream;
                }
            }
        }
        throw new GatewayException("不能访问其他拥有者的数据流");
    }

    private static int countPolicies(List<Item.PrivacyPolicy> policies, long streamId) {
        int count = 0;
        if (policies != null) {
            for (Item.PrivacyPolicy policy : policies) {
                if (policy.getStreamID() == streamId) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isHeader(String[] columns) {
        for (String column : columns) {
            String normalized = column == null ? "" : column.trim().toLowerCase();
            if (normalized.matches(".*[a-zA-Z\\u4e00-\\u9fa5].*")) {
                return true;
            }
        }
        return false;
    }

    private static int findTimestampColumn(String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            String normalized = columns[i] == null ? "" : columns[i].trim().toLowerCase();
            if (normalized.contains("timestamp") || normalized.contains("time") || normalized.contains("date")
                    || normalized.contains("时间") || normalized.contains("日期")) {
                return i;
            }
        }
        return -1;
    }

    private static int findValueColumn(String[] columns, int timestampColumn) {
        String[] preferredNames = {
                "vehicle_flow", "traffic_flow", "traffic_volume", "vehicle_count",
                "vehiclecount", "car_count", "flow", "volume", "count",
                "车流量", "交通流量", "车辆数", "车流", "流量"
        };
        for (String preferredName : preferredNames) {
            for (int i = 0; i < columns.length; i++) {
                if (i == timestampColumn) {
                    continue;
                }
                String normalized = normalizeCsvHeader(columns[i]);
                if (normalized.equals(preferredName) || normalized.contains(preferredName)) {
                    return i;
                }
            }
        }
        for (int i = 0; i < columns.length; i++) {
            if (i == timestampColumn) {
                continue;
            }
            String normalized = normalizeCsvHeader(columns[i]);
            if (normalized.contains("value") || normalized.contains("val") || normalized.contains("metric")
                    || normalized.contains("amount") || normalized.contains("price") || normalized.contains("数值")
                    || normalized.contains("值")) {
                return i;
            }
        }
        for (int i = 0; i < columns.length; i++) {
            if (i != timestampColumn) {
                return i;
            }
        }
        return 0;
    }

    private static String normalizeCsvHeader(String header) {
        return header == null ? "" : header.trim().replace("\"", "").toLowerCase();
    }

    private static long parseTimestamp(String raw, int lineNumber) {
        try {
            long timestamp = Long.parseLong(raw);
            if (Math.abs(timestamp) < 100_000_000_000L) {
                return timestamp * 1000L;
            }
            return timestamp;
        } catch (NumberFormatException ignored) {
        }
        String normalized = raw.replace('/', '-').replace('T', ' ');
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(normalized, formatter);
                return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return OffsetDateTime.parse(raw).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        throw new GatewayException("CSV 第 " + lineNumber + " 行时间格式无法识别：" + raw);
    }

    private static long parseValue(String raw, int lineNumber) {
        try {
            return Math.round(Double.parseDouble(raw));
        } catch (NumberFormatException e) {
            throw new GatewayException("CSV 第 " + lineNumber + " 行数值格式无法识别：" + raw);
        }
    }

    private static class ParsedCsvRow {
        private final long timestamp;
        private final long value;

        private ParsedCsvRow(long timestamp, long value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    private static int intProp(Properties properties, String key) {
        try {
            return Integer.parseInt(properties.getProperty(key, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long longProp(Properties properties, String key) {
        try {
            return Long.parseLong(properties.getProperty(key, "0"));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void ensureLinkedProducer(UserSession session, DataOwnerClient doc, long streamId) throws Exception {
        FrontEndSQL.ProducerBinding binding = sql.getStreamProducer(session.getNumber(), streamId);
        if (binding != null) {
            if (doc.getCurProducerId() != binding.getProducerId()) {
                doc.linkProducer(binding.getProducerId(), binding.getProducerName());
            }
            return;
        }

        List<Equipment> equipments = sql.getEqResults(session.getNumber());
        if (equipments == null || equipments.isEmpty()) {
            throw new GatewayException("该数据流缺少生产者绑定，且当前拥有者没有可用设备；请先注册设备或重新创建数据流");
        }

        Equipment fallback = equipments.get(0);
        long producerId;
        try {
            producerId = Long.parseLong(fallback.getIdnum());
        } catch (NumberFormatException e) {
            throw new GatewayException("该数据流缺少生产者绑定，且设备编号非法：" + fallback.getIdnum(), e);
        }

        String producerName = fallback.getName();
        doc.linkProducer(producerId, producerName);
        sql.upsertStreamProducer(session.getNumber(), streamId, producerId, producerName);
    }

    private void assertOwnedProducer(UserSession session, long producerId, String producerName) {
        List<Equipment> equipments = sql.getEqResults(session.getNumber());
        if (equipments == null) {
            throw new GatewayException("请选择当前拥有者名下的生产者设备");
        }
        String producerIdText = String.valueOf(producerId);
        for (Equipment equipment : equipments) {
            if (producerIdText.equals(equipment.getIdnum())) {
                if (!producerName.isEmpty() && !producerName.equals(equipment.getName())) {
                    throw new GatewayException("生产者设备名称与当前拥有者设备不匹配");
                }
                return;
            }
        }
        throw new GatewayException("不能使用其他拥有者的生产者设备");
    }

    private void assertOwnedStream(UserSession session, long streamId) {
        List<Stream> streams = sql.searchStream(session.getNumber());
        if (streams != null) {
            for (Stream stream : streams) {
                if (stream.getId() == streamId) {
                    return;
                }
            }
        }
        throw new GatewayException("不能访问其他拥有者的数据流");
    }

    private static void assertSameOwner(UserSession session, String ownerId) {
        if (ownerId != null && !ownerId.trim().isEmpty() && !session.getNumber().equals(ownerId.trim())) {
            throw new GatewayException("不能访问其他拥有者的数据流");
        }
    }

    private static String safePathPart(String value) {
        return value == null ? "unknown" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static Path projectPath(String relativePath) {
        Path currentDir = Paths.get("").toAbsolutePath();
        while (currentDir != null) {
            if (Files.isDirectory(currentDir.resolve("web_gateway"))
                    && Files.isDirectory(currentDir.resolve("traffic_stream_producer"))) {
                return currentDir.resolve(relativePath);
            }
            if (Files.isDirectory(currentDir.resolve("pcsig-alfred"))) {
                return currentDir.resolve("pcsig-alfred").resolve(relativePath);
            }
            currentDir = currentDir.getParent();
        }
        return Paths.get(relativePath);
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

    private static class CsvUploadSummary {
        private final String source;
        private final int totalRows;
        private final int validRows;
        private final int ignoredRows;
        private final List<DataPoint> dataPoints;
        private final String timeMode;
        private final boolean sampled;
        private String fileName;
        private String fileSha256;
        private long fileSize;
        private long uploadedAt;

        private CsvUploadSummary(String source, int totalRows, int validRows, int ignoredRows) {
            this(source, totalRows, validRows, ignoredRows, new ArrayList<>(), "simulated", false);
        }

        private CsvUploadSummary(String source, int totalRows, int validRows, int ignoredRows, List<DataPoint> dataPoints) {
            this(source, totalRows, validRows, ignoredRows, dataPoints, "original", false);
        }

        private CsvUploadSummary(String source, int totalRows, int validRows, int ignoredRows,
                                 List<DataPoint> dataPoints, String timeMode) {
            this(source, totalRows, validRows, ignoredRows, dataPoints, timeMode, false);
        }

        private CsvUploadSummary(String source, int totalRows, int validRows, int ignoredRows,
                                 List<DataPoint> dataPoints, String timeMode, boolean sampled) {
            this.source = source;
            this.totalRows = totalRows;
            this.validRows = validRows;
            this.ignoredRows = ignoredRows;
            this.dataPoints = dataPoints;
            this.timeMode = timeMode;
            this.sampled = sampled;
        }
    }

    private static class UploadStatus {
        private final boolean uploaded;
        private final String source;
        private final int totalRows;
        private final int validRows;
        private final int ignoredRows;
        private final long uploadedAt;
        private final String timeMode;
        private final boolean sampled;

        private UploadStatus(boolean uploaded, String source, int totalRows, int validRows, int ignoredRows,
                             long uploadedAt, String timeMode, boolean sampled) {
            this.uploaded = uploaded;
            this.source = source;
            this.totalRows = totalRows;
            this.validRows = validRows;
            this.ignoredRows = ignoredRows;
            this.uploadedAt = uploadedAt;
            this.timeMode = timeMode;
            this.sampled = sampled;
        }

        private static UploadStatus empty() {
            return new UploadStatus(false, null, 0, 0, 0, 0L, "none", false);
        }
    }
}
