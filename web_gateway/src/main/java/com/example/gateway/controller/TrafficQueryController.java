package com.example.gateway.controller;

import com.example.gateway.service.TrafficCsvAnalytics;
import com.example.gateway.service.BlockchainAnchorService;
import com.example.gateway.service.AdminAuditService;
import com.example.gateway.session.SessionManager;
import com.example.gateway.session.UserSession;
import com.example.gateway.support.ApiResponse;
import com.example.gateway.support.GatewayException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;
import sqlConnect.Connect;
import sqlConnect.FrontEndSQL;
import streamHandling.FederationPolicy;
import streamHandling.FederationToken;
import usrs.DataConsumer;
import usrs.DataOwnerClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TrafficQueryController {
    private final SessionManager sessionManager;
    private final BlockchainAnchorService anchors;
    private final AdminAuditService audit;
    private final FrontEndSQL sql = new FrontEndSQL();
    private final TrafficCsvAnalytics analytics = new TrafficCsvAnalytics();

    public TrafficQueryController(SessionManager sessionManager, BlockchainAnchorService anchors,
                                  AdminAuditService audit) {
        this.sessionManager = sessionManager;
        this.anchors = anchors;
        this.audit = audit;
    }

    @PostMapping("/owner/query")
    public ApiResponse<Map<String, Object>> ownerQuery(@RequestHeader("X-Token") String token,
                                                       @RequestBody Map<String, Object> body) {
        UserSession session = sessionManager.requireOwner(token);
        long streamId = lng(body.get("streamId"));
        long startMs = lng(body.get("startTime"));
        long endMs = lng(body.get("endTime"));
        long blockMillis = lng(body.get("blockMillis"));
        if (blockMillis <= 0) blockMillis = lng(body.get("granularityMillis"));
        if (blockMillis <= 0) blockMillis = 60_000L;
        if (streamId == 0 || startMs <= 0 || endMs <= 0 || startMs >= endMs) {
            throw new GatewayException("Owner 查询参数不完整或时间范围无效");
        }
        assertOwnedStream(session, streamId);
        streamHandling.Stream stream = sql.getStream(streamId);
        if (stream == null) {
            throw new GatewayException("未找到对应的数据流");
        }
        Map<String, Object> data = analytics.queryTraffic(session.getUsrName(), stream, startMs, endMs, blockMillis);
        data.put("streamId", String.valueOf(streamId));
        data.put("streamName", stream.getName());
        return ApiResponse.ok(data);
    }

    @PostMapping("/federation/policies")
    public ApiResponse<Map<String, Object>> createFederationPolicy(@RequestHeader("X-Token") String token,
                                                                   @RequestBody Map<String, Object> body) {
        UserSession session = sessionManager.requireOwner(token);
        String consumerName = str(body.get("consumerName"));
        long streamId = lng(body.get("streamId"));
        long startMs = lng(body.get("startTime"));
        long endMs = lng(body.get("endTime"));
        long minGranularity = lng(body.get("minGranularity"));
        String policyName = str(body.get("policyName"));
        if (policyName.isEmpty()) policyName = "联邦策略-" + System.currentTimeMillis();
        if (consumerName.isEmpty() || streamId == 0 || startMs <= 0 || endMs <= 0 || startMs >= endMs) {
            throw new GatewayException("联邦策略参数不完整或时间范围无效");
        }
        assertOwnedStream(session, streamId);
        long policyId;
        DataOwnerClient ownerClient = sessionManager.ownerClient(session);
        try {
            FederationPolicy federationPolicy = ownerClient.createFederationPolicy(
                    consumerName, streamId, new java.util.Date(startMs), new java.util.Date(endMs));
            if (federationPolicy == null) {
                throw new GatewayException("控制器未接受联邦策略");
            }
            policyId = federationPolicy.getFederationPolicyId();
        } catch (Exception e) {
            throw new GatewayException("创建联邦策略失败（请确认 1101/1102 后端已启动）：" + e.getMessage(), e);
        }
        insertMpcPolicy(session.getUsrName(), consumerName, policyName, policyId, streamId, startMs, endMs, minGranularity);
        Map<String, Object> data = new HashMap<>();
        data.put("policyId", String.valueOf(policyId));
        data.put("policyName", policyName);
        return ApiResponse.ok(data);
    }

    @GetMapping("/federation/policies")
    public ApiResponse<List<Map<String, Object>>> listFederationPolicies(@RequestHeader("X-Token") String token,
                                                                         @RequestParam(required = false) String type) {
        UserSession session = sessionManager.requireConsumer(token);
        return ApiResponse.ok(listMpcPolicies(session.getUsrName(), type));
    }

    @GetMapping("/owner/federation/policies")
    public ApiResponse<List<Map<String, Object>>> listOwnerFederationPolicies(
            @RequestHeader("X-Token") String token) {
        UserSession session = sessionManager.requireOwner(token);
        return ApiResponse.ok(listOwnerMpcPolicies(session.getUsrName()));
    }

    @DeleteMapping("/owner/federation/policies/{policyId}")
    public ApiResponse<Map<String, Object>> deleteOwnerFederationPolicy(
            @RequestHeader("X-Token") String token, @PathVariable("policyId") String policyIdText) {
        UserSession session = sessionManager.requireOwner(token);
        long policyId = lng(policyIdText);
        if (policyId == 0) throw new GatewayException("非法的联邦策略 ID");
        int deleted = executeUpdate("DELETE FROM policy_mpc WHERE policy_id=? AND owner_name=?",
                policyId, session.getUsrName());
        if (deleted != 1) throw new GatewayException("联邦策略不存在或不属于当前数据拥有者");
        Map<String, Object> data = new HashMap<>();
        data.put("policyId", String.valueOf(policyId)); data.put("deleted", true);
        return ApiResponse.ok(data);
    }

    @PutMapping("/owner/federation/policies/{policyId}")
    public ApiResponse<Map<String, Object>> updateOwnerFederationPolicy(
            @RequestHeader("X-Token") String token, @PathVariable("policyId") String policyIdText,
            @RequestBody Map<String, Object> body) {
        UserSession session = sessionManager.requireOwner(token);
        long policyId = lng(policyIdText); long streamId = lng(body.get("streamId"));
        long startMs = lng(body.get("startTime")); long endMs = lng(body.get("endTime"));
        long minGranularity = lng(body.get("minGranularity"));
        String consumerName = str(body.get("consumerName")); String policyName = str(body.get("policyName"));
        if (policyId == 0 || streamId == 0 || consumerName.isEmpty() || policyName.isEmpty()
                || startMs <= 0 || endMs <= 0 || startMs >= endMs) {
            throw new GatewayException("联邦策略参数不完整或时间范围无效");
        }
        assertOwnedStream(session, streamId);
        int updated = executeUpdate("UPDATE policy_mpc SET consumer_name=?,stream_id=?,p_starttime=?,p_endtime=?,mingranularity=?,p_name=? WHERE policy_id=? AND owner_name=?",
                consumerName, streamId, startMs, endMs, minGranularity <= 0 ? 1L : minGranularity,
                policyName, policyId, session.getUsrName());
        if (updated != 1) throw new GatewayException("联邦策略不存在或不属于当前数据拥有者");
        Map<String, Object> data = new HashMap<>();
        data.put("policyId", String.valueOf(policyId)); data.put("updated", true);
        return ApiResponse.ok(data);
    }

    @PostMapping("/federation/query")
    public ApiResponse<Map<String, Object>> federationQuery(@RequestHeader("X-Token") String token,
                                                            @RequestBody Map<String, Object> body) {
        UserSession session = sessionManager.requireConsumer(token);
        List<Long> policyIds = new ArrayList<>(new LinkedHashSet<>(lngList(body.get("policyIds"))));
        long startMs = lng(body.get("startTime"));
        long endMs = lng(body.get("endTime"));
        if (policyIds.size() < 2) {
            recordFederationDenied(session, policyIds, "联邦查询至少需要两个策略");
            throw new GatewayException("联邦查询至少需要选择两个策略/数据流");
        }
        List<Map<String, Object>> policies = listMpcPolicies(session.getUsrName(), null);
        List<TrafficCsvAnalytics.FederationStream> streams = new ArrayList<>();
        ArrayList<Pair<String, Long>> nameAndStreamList = new ArrayList<>();
        List<Long> authorizedPolicyIds = new ArrayList<>();
        List<Long> streamIds = new ArrayList<>();
        long commonStart = Long.MIN_VALUE;
        long commonEnd = Long.MAX_VALUE;
        for (Map<String, Object> policy : policies) {
            long policyId = lng(policy.get("policyId"));
            if (!policyIds.contains(policyId)) continue;
            long policyStart = lng(policy.get("startTime"));
            long policyEnd = lng(policy.get("endTime"));
            commonStart = Math.max(commonStart, policyStart);
            commonEnd = Math.min(commonEnd, policyEnd);
            long streamId = lng(policy.get("streamId"));
            String ownerName = str(policy.get("ownerName"));
            streamHandling.Stream stream = sql.getStream(streamId);
            if (stream != null) {
                streams.add(new TrafficCsvAnalytics.FederationStream(ownerName, policyId, stream));
                nameAndStreamList.add(Pair.of(ownerName, streamId));
                authorizedPolicyIds.add(policyId);
                streamIds.add(streamId);
            }
        }
        if (streams.size() < 2 || authorizedPolicyIds.size() != policyIds.size()) {
            recordFederationDenied(session, policyIds, "策略越权、失效或不存在；chainAction=NONE");
            throw new GatewayException("存在无权访问、已失效或不存在的联邦策略，查询已拒绝且不会上链");
        }
        if (startMs <= 0) startMs = commonStart;
        if (endMs <= 0) endMs = commonEnd;
        if (startMs < commonStart || endMs > commonEnd || startMs >= endMs) {
            recordFederationDenied(session, policyIds, "查询时间超出公共授权范围；chainAction=NONE");
            throw new GatewayException("联邦查询时间必须落在所选策略的公共授权区间内");
        }
        FederationToken tokenResult;
        try {
            DataConsumer dataConsumer = sessionManager.consumer(session);
            tokenResult = dataConsumer.getFederationToken(session.getUsrName(), nameAndStreamList, startMs, endMs);
        } catch (Exception e) {
            recordFederationDenied(session, policyIds, "控制器授权校验异常：" + safeReason(e) + "；chainAction=NONE");
            throw new GatewayException("联邦授权校验失败，查询已终止且不会上链：" + e.getMessage(), e);
        }
        if (tokenResult == null) {
            recordFederationDenied(session, policyIds, "控制器未颁发联邦令牌；chainAction=NONE");
            throw new GatewayException("控制器未颁发联邦令牌，查询已终止且不会上链");
        }
        Map<String, Object> result = analytics.federation(streams, startMs, endMs);
        result.put("tokenIssued", true);
        result.put("effectiveStartTime", startMs);
        result.put("effectiveEndTime", endMs);
        Map<String, Object> credential = anchors.recordFederationExecution(
                session.getUsrName(), authorizedPolicyIds, streamIds, startMs, endMs, result);
        result.put("credential", credential);
        audit.record(String.valueOf(credential.get("traceId")), session, "FEDERATION_QUERY_AUTHORIZED",
                "POST", "/api/federation/query", "federation-query",
                String.valueOf(credential.get("businessId")),
                "授权通过；policies=" + authorizedPolicyIds + "；streams=" + streamIds +
                        "；range=" + startMs + "~" + endMs + "；chainAction=QUEUED；credentialStatus=PENDING",
                true, 0, null);
        return ApiResponse.ok(result);
    }

    private void recordFederationDenied(UserSession session, List<Long> policyIds, String reason) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        audit.record(traceId, session, "FEDERATION_QUERY_DENIED", "POST", "/api/federation/query",
                "federation-query", policyIds.toString(),
                "授权拒绝；policies=" + policyIds + "；reason=" + reason + "；blockchainTransaction=NOT_CREATED",
                false, 0, null);
    }

    private static String safeReason(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) return e.getClass().getSimpleName();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

    private List<Map<String, Object>> listMpcPolicies(String consumerName, String type) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sqlText = "SELECT pm.*, s.name AS stream_name, s.description AS stream_description " +
                "FROM policy_mpc pm LEFT JOIN stream s ON pm.stream_id = s.id WHERE pm.consumer_name = ?";
        boolean filterByType = type != null && !type.trim().isEmpty();
        if (filterByType) {
            sqlText += " AND s.description = ?";
        }
        try (Connection connection = Connect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlText)) {
            statement.setString(1, consumerName);
            if (filterByType) statement.setString(2, type.trim());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ownerName", rs.getString("owner_name"));
                    row.put("consumerName", rs.getString("consumer_name"));
                    row.put("policyId", String.valueOf(rs.getLong("policy_id")));
                    row.put("streamId", String.valueOf(rs.getLong("stream_id")));
                    row.put("streamName", rs.getString("stream_name"));
                    row.put("streamType", rs.getString("stream_description"));
                    row.put("startTime", rs.getLong("p_starttime"));
                    row.put("endTime", rs.getLong("p_endtime"));
                    row.put("minGranularity", rs.getLong("mingranularity"));
                    row.put("policyName", safeColumn(rs, "p_name", "联邦策略 " + rs.getLong("policy_id")));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            throw new GatewayException("读取联邦策略失败：" + e.getMessage(), e);
        }
        return result;
    }

    private List<Map<String, Object>> listOwnerMpcPolicies(String ownerName) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sqlText = "SELECT pm.*,s.name AS stream_name,s.description AS stream_description " +
                "FROM policy_mpc pm LEFT JOIN stream s ON pm.stream_id=s.id WHERE pm.owner_name=? ORDER BY pm.p_starttime DESC";
        try (Connection connection = Connect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlText)) {
            statement.setString(1, ownerName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ownerName", rs.getString("owner_name"));
                    row.put("consumerName", rs.getString("consumer_name"));
                    row.put("policyId", String.valueOf(rs.getLong("policy_id")));
                    row.put("streamId", String.valueOf(rs.getLong("stream_id")));
                    row.put("streamName", rs.getString("stream_name"));
                    row.put("streamType", rs.getString("stream_description"));
                    row.put("startTime", rs.getLong("p_starttime")); row.put("endTime", rs.getLong("p_endtime"));
                    row.put("minGranularity", rs.getLong("mingranularity"));
                    row.put("policyName", safeColumn(rs, "p_name", "联邦策略 " + rs.getLong("policy_id")));
                    row.put("policyType", "federation"); result.add(row);
                }
            }
        } catch (SQLException e) { throw new GatewayException("读取拥有者联邦策略失败：" + e.getMessage(), e); }
        return result;
    }

    private static int executeUpdate(String sqlText, Object... params) {
        try (Connection connection = Connect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlText)) {
            for (int i = 0; i < params.length; i++) statement.setObject(i + 1, params[i]);
            return statement.executeUpdate();
        } catch (SQLException e) { throw new GatewayException("联邦策略操作失败：" + e.getMessage(), e); }
    }

    private static void insertMpcPolicy(String ownerName, String consumerName, String policyName, long policyId,
                                        long streamId, long startMs, long endMs, long minGranularity) {
        String sqlText = "INSERT INTO policy_mpc (owner_name, consumer_name, policy_id, stream_id, p_starttime, p_endtime, mingranularity, p_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Connect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlText)) {
            statement.setString(1, ownerName);
            statement.setString(2, consumerName);
            statement.setLong(3, policyId);
            statement.setLong(4, streamId);
            statement.setLong(5, startMs);
            statement.setLong(6, endMs);
            statement.setLong(7, minGranularity <= 0 ? 1L : minGranularity);
            statement.setString(8, policyName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new GatewayException("保存联邦策略失败：" + e.getMessage(), e);
        }
    }

    private void assertOwnedStream(UserSession session, long streamId) {
        List<Item.Stream> streams = sql.searchStream(session.getNumber());
        if (streams != null) {
            for (Item.Stream stream : streams) {
                if (stream.getId() == streamId) {
                    return;
                }
            }
        }
        throw new GatewayException("不能操作其他拥有者的数据流");
    }

    private static String safeColumn(ResultSet rs, String column, String fallback) {
        try {
            String value = rs.getString(column);
            return value == null || value.isBlank() ? fallback : value;
        } catch (SQLException e) {
            return fallback;
        }
    }

    private static List<Long> lngList(Object value) {
        List<Long> result = new ArrayList<>();
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                long parsed = lng(item);
                if (parsed != 0) result.add(parsed);
            }
        }
        return result;
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
}
