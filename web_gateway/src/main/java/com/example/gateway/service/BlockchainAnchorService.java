package com.example.gateway.service;

import com.example.gateway.support.GatewayException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sqlConnect.Connect;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BlockchainAnchorService {
    private static final long PROCESSING_TIMEOUT_MS = 120_000L;
    private static final ObjectMapper CANONICAL_MAPPER = new ObjectMapper()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    private final EthereumAnchorClient ethereum;
    private final StreamUploadFingerprintService fingerprints;

    public BlockchainAnchorService(EthereumAnchorClient ethereum, StreamUploadFingerprintService fingerprints) {
        this.ethereum = ethereum;
        this.fingerprints = fingerprints;
    }

    @PostConstruct
    public void initialize() {
        try (Connection connection = Connect.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS blockchain_anchor (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "trace_id VARCHAR(64) NOT NULL, business_type VARCHAR(48) NOT NULL, business_id VARCHAR(255) NOT NULL," +
                    "payload_json LONGTEXT NULL, payload_sha256 CHAR(64) NULL, status VARCHAR(24) NOT NULL DEFAULT 'PENDING'," +
                    "retry_count INT NOT NULL DEFAULT 0, max_retries INT NOT NULL DEFAULT 5, next_attempt_at BIGINT NOT NULL DEFAULT 0," +
                    "locked_at BIGINT NULL, last_error VARCHAR(2000) NULL, chain_id BIGINT NULL, from_address VARCHAR(128) NULL," +
                    "transaction_hash VARCHAR(128) NULL, block_number BIGINT NULL, confirmed_at BIGINT NULL," +
                    "created_at BIGINT NOT NULL, updated_at BIGINT NOT NULL," +
                    "UNIQUE KEY uk_anchor_business(business_type,business_id), INDEX idx_anchor_work(status,next_attempt_at)," +
                    "INDEX idx_anchor_trace(trace_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS computation_trace (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, trace_id VARCHAR(64) NOT NULL, stream_id BIGINT NOT NULL," +
                    "stage VARCHAR(64) NOT NULL, start_time BIGINT NULL, end_time BIGINT NULL, algorithm VARCHAR(128) NULL," +
                    "input_sha256 CHAR(64) NULL, output_sha256 CHAR(64) NULL, anomaly TINYINT(1) NOT NULL DEFAULT 0," +
                    "metadata_json LONGTEXT NULL, created_at BIGINT NOT NULL," +
                    "UNIQUE KEY uk_computation_stage(trace_id,stage), INDEX idx_computation_stream(stream_id,created_at)) " +
                    "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS federation_query_execution (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, trace_id VARCHAR(64) NOT NULL," +
                    "consumer_name VARCHAR(255) NOT NULL, policy_ids_json LONGTEXT NOT NULL, stream_ids_json LONGTEXT NOT NULL," +
                    "start_time BIGINT NOT NULL, end_time BIGINT NOT NULL, token_issued TINYINT(1) NOT NULL DEFAULT 1," +
                    "input_sha256 CHAR(64) NOT NULL, output_sha256 CHAR(64) NOT NULL, result_json LONGTEXT NOT NULL," +
                    "created_at BIGINT NOT NULL, UNIQUE KEY uk_federation_query_trace(trace_id)," +
                    "INDEX idx_federation_query_consumer(consumer_name,created_at)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            createInsertTrigger(connection, "trg_policy_anchor", "policy", "POLICY", "NEW.policy_id");
            statement.executeUpdate("DROP TRIGGER IF EXISTS trg_policy_mpc_anchor");
            statement.executeUpdate("DELETE FROM blockchain_anchor WHERE business_type='FEDERATION_POLICY' AND status<>'CONFIRMED'");
            backfillPolicies(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("初始化区块链存证表失败：" + e.getMessage(), e);
        }
    }

    private void createInsertTrigger(Connection connection, String trigger, String table,
                                     String businessType, String idExpression) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TRIGGER IF EXISTS " + trigger);
        }
        String sql = "CREATE TRIGGER " + trigger + " AFTER INSERT ON " + table + " FOR EACH ROW " +
                "INSERT IGNORE INTO blockchain_anchor(trace_id,business_type,business_id,status,created_at,updated_at) " +
                "VALUES(REPLACE(UUID(),'-',''),'" + businessType + "',CAST(" + idExpression + " AS CHAR),'PENDING'," +
                "CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS UNSIGNED)," +
                "CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS UNSIGNED))";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void backfillPolicies(Connection connection) throws SQLException {
        long now = System.currentTimeMillis();
        for (String[] source : java.util.Collections.singletonList(new String[]{"POLICY", "policy"})) {
            String sql = "INSERT IGNORE INTO blockchain_anchor(trace_id,business_type,business_id,status,created_at,updated_at) " +
                    "SELECT REPLACE(UUID(),'-',''),?,CAST(policy_id AS CHAR),'PENDING',?,? FROM " + source[1];
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, source[0]); statement.setLong(2, now); statement.setLong(3, now);
                statement.executeUpdate();
            }
        }
    }

    @Scheduled(fixedDelayString = "${blockchain.worker.delay-ms:10000}", initialDelayString = "${blockchain.worker.initial-delay-ms:3000}")
    public void processPending() {
        if (ethereum.isConfigured()) {
            update("UPDATE blockchain_anchor SET status='PENDING',next_attempt_at=0,last_error=NULL,updated_at=? WHERE status='WAITING_CONFIG'",
                    System.currentTimeMillis());
        }
        recoverTimedOut();
        for (Long id : claimBatch(5)) processOne(id);
    }

    private void recoverTimedOut() {
        update("UPDATE blockchain_anchor SET status='PENDING',locked_at=NULL,last_error='处理超时，已自动恢复',updated_at=? " +
                        "WHERE status='PROCESSING' AND locked_at<?", System.currentTimeMillis(),
                System.currentTimeMillis() - PROCESSING_TIMEOUT_MS);
    }

    private List<Long> claimBatch(int limit) {
        List<Long> candidates = new ArrayList<>();
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM blockchain_anchor WHERE status IN ('PENDING','FAILED') AND retry_count<max_retries " +
                        "AND next_attempt_at<=? ORDER BY id LIMIT ?")) {
            statement.setLong(1, System.currentTimeMillis()); statement.setInt(2, limit);
            try (ResultSet rs = statement.executeQuery()) { while (rs.next()) candidates.add(rs.getLong(1)); }
        } catch (SQLException e) { return List.of(); }
        List<Long> claimed = new ArrayList<>();
        for (Long id : candidates) {
            if (update("UPDATE blockchain_anchor SET status='PROCESSING',locked_at=?,updated_at=? " +
                            "WHERE id=? AND status IN ('PENDING','FAILED')", System.currentTimeMillis(),
                    System.currentTimeMillis(), id) == 1) claimed.add(id);
        }
        return claimed;
    }

    private void processOne(long id) {
        Map<String, Object> anchor = one("SELECT * FROM blockchain_anchor WHERE id=?", id);
        if (anchor == null) return;
        try {
            Map<String, Object> payload = loadPayload(String.valueOf(anchor.get("business_type")),
                    String.valueOf(anchor.get("business_id")));
            String json = canonicalJson(payload);
            String hash = sha256(json);
            update("UPDATE blockchain_anchor SET payload_json=?,payload_sha256=?,updated_at=? WHERE id=?",
                    json, hash, System.currentTimeMillis(), id);
            if (!ethereum.isConfigured()) {
                update("UPDATE blockchain_anchor SET status='WAITING_CONFIG',locked_at=NULL,next_attempt_at=0," +
                                "last_error='区块链未配置：请设置 BLOCKCHAIN_RPC_URL、BLOCKCHAIN_PRIVATE_KEY 和 BLOCKCHAIN_CHAIN_ID',updated_at=? WHERE id=?",
                        System.currentTimeMillis(), id);
                return;
            }
            EthereumAnchorClient.AnchorReceipt receipt = ethereum.anchor(hash);
            update("UPDATE blockchain_anchor SET status='CONFIRMED',locked_at=NULL,last_error=NULL,chain_id=?," +
                            "from_address=?,transaction_hash=?,block_number=?,confirmed_at=?,updated_at=? WHERE id=?",
                    receipt.chainId(), receipt.fromAddress(), receipt.transactionHash(), receipt.blockNumber(),
                    System.currentTimeMillis(), System.currentTimeMillis(), id);
        } catch (Exception e) {
            int retry = number(anchor.get("retry_count")) + 1;
            long delay = Math.min(300_000L, 5_000L * (1L << Math.min(retry, 6)));
            update("UPDATE blockchain_anchor SET status='FAILED',retry_count=?,locked_at=NULL,next_attempt_at=?,last_error=?,updated_at=? WHERE id=?",
                    retry, System.currentTimeMillis() + delay, truncate(e.getMessage(), 1900), System.currentTimeMillis(), id);
        }
    }

    private Map<String, Object> loadPayload(String type, String businessId) {
        String table;
        String granularity;
        if ("POLICY".equals(type)) { table = "policy"; granularity = "multiple"; }
        else if ("FEDERATION_POLICY".equals(type)) { table = "policy_mpc"; granularity = "mingranularity"; }
        else if ("FEDERATION_QUERY_EXECUTION".equals(type)) return one(
                "SELECT trace_id,consumer_name,policy_ids_json,stream_ids_json,start_time,end_time,token_issued," +
                        "input_sha256,output_sha256,result_json,created_at FROM federation_query_execution WHERE id=?", businessId);
        else if ("COMPUTATION_TRACE".equals(type)) return one("SELECT trace_id,CAST(stream_id AS CHAR) stream_id,stage,start_time,end_time,algorithm,input_sha256,output_sha256,anomaly,metadata_json FROM computation_trace WHERE id=?", businessId);
        else throw new GatewayException("不支持的存证类型：" + type);
        Map<String, Object> row = one("SELECT '" + type + "' record_type,CAST(policy_id AS CHAR) policy_id," +
                "CAST(stream_id AS CHAR) stream_id,owner_name,consumer_name,p_starttime,p_endtime," + granularity +
                " granularity,p_name policy_name FROM " + table + " WHERE policy_id=?", businessId);
        if (row == null) throw new GatewayException("存证源记录不存在");
        long streamId = Long.parseLong(String.valueOf(row.get("stream_id")));
        Map<String, Object> upload = fingerprints.latestForStream(streamId);
        row.put("credential_type", "DATA_AUTHORIZATION");
        row.put("csv_fingerprint", upload);
        row.put("credential_scope", upload == null
                ? "POLICY_ONLY_LEGACY" : "CSV_FINGERPRINT_AND_ACCESS_POLICY");
        return row;
    }

    public Map<String, Object> latestUploadFingerprint(long streamId) {
        return fingerprints.latestForStream(streamId);
    }

    public String recordComputation(long streamId, long start, long end, List<Map<String, Object>> process,
                                    List<Map<String, Object>> anomalies, List<Map<String, Object>> blocks) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("anomalies", anomalies); metadata.put("blocks", blocks); metadata.put("process", process);
        String metadataJson = canonicalJson(metadata);
        String inputHash = sha256(canonicalJson(blocks));
        String outputHash = sha256(metadataJson);
        long now = System.currentTimeMillis();
        try (Connection connection = Connect.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement trace = connection.prepareStatement(
                    "INSERT INTO computation_trace(trace_id,stream_id,stage,start_time,end_time,algorithm,input_sha256,output_sha256,anomaly,metadata_json,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                trace.setString(1, traceId); trace.setLong(2, streamId); trace.setString(3, "ANOMALY_AGGREGATION");
                trace.setLong(4, start); trace.setLong(5, end); trace.setString(6, "AES-PRG + Homomorphic MAC + Z-score");
                trace.setString(7, inputHash); trace.setString(8, outputHash); trace.setBoolean(9, !anomalies.isEmpty());
                trace.setString(10, metadataJson); trace.setLong(11, now); trace.executeUpdate();
                try (ResultSet keys = trace.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("未取得计算追踪编号");
                    try (PreparedStatement anchor = connection.prepareStatement(
                            "INSERT INTO blockchain_anchor(trace_id,business_type,business_id,status,created_at,updated_at) VALUES(?,?,?,?,?,?)")) {
                        anchor.setString(1, traceId); anchor.setString(2, "COMPUTATION_TRACE");
                        anchor.setString(3, String.valueOf(keys.getLong(1))); anchor.setString(4, "PENDING");
                        anchor.setLong(5, now); anchor.setLong(6, now); anchor.executeUpdate();
                    }
                }
                connection.commit();
            } catch (Exception e) { connection.rollback(); throw e; }
        } catch (Exception e) { throw new GatewayException("保存计算溯源失败：" + e.getMessage(), e); }
        return traceId;
    }

    public Map<String, Object> recordFederationExecution(String consumerName, List<Long> policyIds,
                                                          List<Long> streamIds, long start, long end,
                                                          Map<String, Object> result) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String policyJson = canonicalJson(policyIds);
        String streamJson = canonicalJson(streamIds);
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("consumer", consumerName); input.put("policyIds", policyIds); input.put("streamIds", streamIds);
        input.put("startTime", start); input.put("endTime", end);
        String inputHash = sha256(canonicalJson(input));
        String resultJson = canonicalJson(result);
        String outputHash = sha256(resultJson);
        long now = System.currentTimeMillis();
        long executionId;
        try (Connection connection = Connect.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement execution = connection.prepareStatement(
                    "INSERT INTO federation_query_execution(trace_id,consumer_name,policy_ids_json,stream_ids_json,start_time,end_time,token_issued,input_sha256,output_sha256,result_json,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                execution.setString(1, traceId); execution.setString(2, consumerName);
                execution.setString(3, policyJson); execution.setString(4, streamJson);
                execution.setLong(5, start); execution.setLong(6, end); execution.setBoolean(7, true);
                execution.setString(8, inputHash); execution.setString(9, outputHash);
                execution.setString(10, resultJson); execution.setLong(11, now); execution.executeUpdate();
                try (ResultSet keys = execution.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("未取得联邦查询执行编号");
                    executionId = keys.getLong(1);
                }
                try (PreparedStatement anchor = connection.prepareStatement(
                        "INSERT INTO blockchain_anchor(trace_id,business_type,business_id,status,created_at,updated_at) VALUES(?,?,?,?,?,?)")) {
                    anchor.setString(1, traceId); anchor.setString(2, "FEDERATION_QUERY_EXECUTION");
                    anchor.setString(3, String.valueOf(executionId)); anchor.setString(4, "PENDING");
                    anchor.setLong(5, now); anchor.setLong(6, now); anchor.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) { connection.rollback(); throw e; }
        } catch (Exception e) {
            throw new GatewayException("保存联邦查询执行凭证失败：" + e.getMessage(), e);
        }
        Map<String, Object> credential = new LinkedHashMap<>();
        credential.put("traceId", traceId); credential.put("businessType", "FEDERATION_QUERY_EXECUTION");
        credential.put("businessId", String.valueOf(executionId)); credential.put("status", "PENDING");
        credential.put("inputSha256", inputHash); credential.put("outputSha256", outputHash);
        return credential;
    }

    public List<Map<String, Object>> list(int limit, String status, String type) {
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        StringBuilder sql = new StringBuilder("SELECT id,trace_id,business_type,business_id,payload_sha256,status,retry_count,max_retries,next_attempt_at,last_error,chain_id,from_address,transaction_hash,block_number,confirmed_at,created_at,updated_at FROM blockchain_anchor WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) { sql.append(" AND status=?"); params.add(status); }
        if (type != null && !type.isBlank()) { sql.append(" AND business_type=?"); params.add(type); }
        sql.append(" ORDER BY id DESC LIMIT ").append(safeLimit);
        return rows(sql.toString(), params.toArray());
    }

    public Map<String, Object> detail(long id) {
        Map<String, Object> row = one("SELECT * FROM blockchain_anchor WHERE id=?", id);
        if (row == null) throw new GatewayException("存证记录不存在");
        String payload = (String) row.get("payload_json");
        String stored = (String) row.get("payload_sha256");
        row.put("recomputed_sha256", payload == null ? null : sha256(payload));
        row.put("verified", payload != null && stored != null && stored.equals(sha256(payload)));
        row.put("configured", ethereum.isConfigured());
        row.put("explorer_url", ethereum.explorerUrl((String) row.get("transaction_hash")));
        if ("CONFIRMED".equals(row.get("status")) && ethereum.isConfigured()) {
            try {
                row.put("on_chain_verified", ethereum.verify((String) row.get("transaction_hash"), stored));
            } catch (Exception e) {
                row.put("on_chain_verified", false);
                row.put("on_chain_verification_error", truncate(e.getMessage(), 500));
            }
        } else {
            row.put("on_chain_verified", false);
        }
        return row;
    }

    public Map<String, Object> findByBusiness(String type, String businessId) {
        return one("SELECT id,trace_id,business_type,business_id,payload_sha256,status,retry_count,max_retries," +
                "last_error,chain_id,from_address,transaction_hash,block_number,confirmed_at,created_at,updated_at " +
                "FROM blockchain_anchor WHERE business_type=? AND business_id=?", type, businessId);
    }

    public void retry(long id) {
        if (update("UPDATE blockchain_anchor SET status='PENDING',retry_count=0,next_attempt_at=0,locked_at=NULL,last_error=NULL,updated_at=? WHERE id=? AND status IN ('PENDING','FAILED','WAITING_CONFIG')",
                System.currentTimeMillis(), id) != 1) throw new GatewayException("记录不存在或正在处理中");
    }

    public long count(String status) {
        Map<String, Object> row = one("SELECT COUNT(*) total FROM blockchain_anchor" +
                (status == null ? "" : " WHERE status=?"), status == null ? new Object[]{} : new Object[]{status});
        return row == null ? 0 : ((Number) row.get("total")).longValue();
    }

    public static String canonicalJson(Object value) {
        try { return CANONICAL_MAPPER.writeValueAsString(value); }
        catch (JsonProcessingException e) { throw new IllegalArgumentException("无法规范化存证内容", e); }
    }

    public static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException("当前 Java 环境不支持 SHA-256", e); }
    }

    private Map<String, Object> one(String sql, Object... params) {
        List<Map<String, Object>> rows = rows(sql, params); return rows.isEmpty() ? null : rows.get(0);
    }

    private List<Map<String, Object>> rows(String sql, Object... params) {
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params); try (ResultSet rs = statement.executeQuery()) {
                List<Map<String, Object>> result = new ArrayList<>(); ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) { Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) row.put(meta.getColumnLabel(i), rs.getObject(i)); result.add(row); }
                return result;
            }
        } catch (SQLException e) { throw new GatewayException("读取存证记录失败：" + e.getMessage(), e); }
    }

    private int update(String sql, Object... params) {
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params); return statement.executeUpdate();
        } catch (SQLException e) { throw new GatewayException("更新存证记录失败：" + e.getMessage(), e); }
    }

    private static void bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) statement.setObject(i + 1, params[i]);
    }
    private static int number(Object value) { return value instanceof Number ? ((Number) value).intValue() : 0; }
    private static String truncate(String value, int max) {
        if (value == null) return "未知错误"; return value.length() <= max ? value : value.substring(0, max);
    }
}
