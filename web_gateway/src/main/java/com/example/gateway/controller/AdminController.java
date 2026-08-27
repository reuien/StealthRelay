package com.example.gateway.controller;

import com.example.gateway.service.AdminAuditService;
import com.example.gateway.service.AdminTraceService;
import com.example.gateway.service.BlockchainAnchorService;
import com.example.gateway.session.SessionManager;
import com.example.gateway.session.UserSession;
import com.example.gateway.support.ApiResponse;
import com.example.gateway.support.GatewayException;
import org.springframework.web.bind.annotation.*;
import sqlConnect.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final SessionManager sessions;
    private final AdminAuditService audit;
    private final AdminTraceService traces;
    private final BlockchainAnchorService anchors;

    public AdminController(SessionManager sessions, AdminAuditService audit, AdminTraceService traces,
                           BlockchainAnchorService anchors) {
        this.sessions = sessions; this.audit = audit; this.traces = traces; this.anchors = anchors;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@RequestHeader("X-Token") String token) {
        sessions.requireAdmin(token);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("users", scalar("SELECT COUNT(*) FROM custom"));
        data.put("streams", scalar("SELECT COUNT(*) FROM stream"));
        data.put("policies", scalar("SELECT (SELECT COUNT(*) FROM policy)+(SELECT COUNT(*) FROM policy_mpc)"));
        data.put("auditEvents", scalar("SELECT COUNT(*) FROM admin_audit_log"));
        data.put("disabledUsers", scalar("SELECT COUNT(*) FROM user_account_status WHERE disabled=1"));
        data.put("anchors", anchors.count(null));
        data.put("confirmedAnchors", anchors.count("CONFIRMED"));
        data.put("failedAnchors", anchors.count("FAILED"));
        return ApiResponse.ok(data);
    }

    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> users(@RequestHeader("X-Token") String token) {
        sessions.requireAdmin(token);
        return ApiResponse.ok(rows("SELECT c.number,c.usr_name,c.identity,COALESCE(us.disabled,0) disabled,us.reason," +
                "(SELECT COUNT(*) FROM owner_stream os WHERE os.owner_id=c.number) stream_count " +
                "FROM custom c LEFT JOIN user_account_status us ON us.user_number=c.number ORDER BY c.identity,c.number"));
    }

    @PutMapping("/users/{number}/status")
    public ApiResponse<Void> userStatus(@RequestHeader("X-Token") String token, @PathVariable String number,
                                        @RequestBody Map<String, Object> body) {
        UserSession admin = sessions.requireAdmin(token); assertNotAdmin(number);
        boolean disabled = Boolean.parseBoolean(String.valueOf(body.getOrDefault("disabled", false)));
        String reason = String.valueOf(body.getOrDefault("reason", "管理员操作"));
        update("INSERT INTO user_account_status(user_number,disabled,updated_at,updated_by,reason) VALUES(?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE disabled=VALUES(disabled),updated_at=VALUES(updated_at),updated_by=VALUES(updated_by),reason=VALUES(reason)",
                number, disabled, System.currentTimeMillis(), admin.getNumber(), reason);
        recordAdmin(admin, "USER_STATUS", "user", number, "disabled=" + disabled + ", reason=" + reason);
        return ApiResponse.ok();
    }

    @DeleteMapping("/users/{number}")
    public ApiResponse<Void> deleteUser(@RequestHeader("X-Token") String token, @PathVariable String number) {
        UserSession admin = sessions.requireAdmin(token); assertNotAdmin(number);
        List<Map<String, Object>> ids = rows("SELECT stream_id FROM owner_stream WHERE owner_id=?", number);
        for (Map<String, Object> row : ids) deleteStreamRecords(String.valueOf(row.get("stream_id")));
        update("DELETE FROM custom WHERE number=?", number);
        recordAdmin(admin, "USER_DELETE", "user", number, "deleted streams=" + ids.size());
        return ApiResponse.ok();
    }

    @GetMapping("/streams")
    public ApiResponse<List<Map<String, Object>>> streams(@RequestHeader("X-Token") String token) {
        sessions.requireAdmin(token);
        return ApiResponse.ok(rows("SELECT CAST(s.id AS CHAR) id,s.name,s.description,s.starttime,s.endtime,s.mingranularity,s.granularity," +
                "os.owner_id,c.usr_name owner_name,sp.producer_id,sp.producer_name," +
                "(SELECT COUNT(*) FROM policy p WHERE p.stream_id=s.id) policy_count," +
                "(SELECT COUNT(*) FROM policy_mpc pm WHERE pm.stream_id=s.id) federation_count " +
                "FROM stream s LEFT JOIN owner_stream os ON os.stream_id=s.id LEFT JOIN custom c ON c.number=os.owner_id " +
                "LEFT JOIN stream_producer sp ON sp.stream_id=s.id ORDER BY s.starttime DESC"));
    }

    @DeleteMapping("/streams/{id}")
    public ApiResponse<Void> deleteStream(@RequestHeader("X-Token") String token, @PathVariable String id) {
        UserSession admin = sessions.requireAdmin(token); deleteStreamRecords(id);
        recordAdmin(admin, "STREAM_DELETE", "stream", id, "database and relationships deleted; remote encrypted store cleanup is not asserted");
        return ApiResponse.ok();
    }

    @GetMapping("/policies")
    public ApiResponse<List<Map<String, Object>>> policies(@RequestHeader("X-Token") String token) {
        sessions.requireAdmin(token);
        return ApiResponse.ok(rows("SELECT 'normal' policy_type,CAST(policy_id AS CHAR) policy_id,CAST(stream_id AS CHAR) stream_id," +
                "owner_name,consumer_name,p_starttime,p_endtime,multiple granularity FROM policy UNION ALL " +
                "SELECT 'federation',CAST(policy_id AS CHAR),CAST(stream_id AS CHAR),owner_name,consumer_name,p_starttime,p_endtime,mingranularity FROM policy_mpc " +
                "ORDER BY p_starttime DESC"));
    }

    @GetMapping("/policies/{type}/{id}")
    public ApiResponse<Map<String, Object>> policyDetail(@RequestHeader("X-Token") String token,
                                                          @PathVariable String type,
                                                          @PathVariable String id) {
        UserSession admin = sessions.requireAdmin(token);
        boolean federation = "federation".equalsIgnoreCase(type);
        if (!federation && !"normal".equalsIgnoreCase(type)) {
            throw new GatewayException("未知策略类型");
        }
        String table = federation ? "policy_mpc" : "policy";
        String granularityColumn = federation ? "mingranularity" : "multiple";
        List<Map<String, Object>> matches = rows(
                "SELECT ? policy_type,CAST(p.policy_id AS CHAR) policy_id,CAST(p.stream_id AS CHAR) stream_id," +
                        "p.owner_name,p.consumer_name,p.p_starttime,p.p_endtime,p." + granularityColumn + " granularity," +
                        "s.name stream_name,s.description stream_description,s.starttime stream_starttime," +
                        "s.endtime stream_endtime,s.mingranularity stream_min_granularity,s.granularity stream_granularity," +
                        "os.owner_id,sp.producer_id,sp.producer_name " +
                        "FROM " + table + " p LEFT JOIN stream s ON s.id=p.stream_id " +
                        "LEFT JOIN owner_stream os ON os.stream_id=p.stream_id " +
                        "LEFT JOIN stream_producer sp ON sp.stream_id=p.stream_id WHERE p.policy_id=?",
                federation ? "federation" : "normal", id);
        if (matches.isEmpty()) throw new GatewayException("策略不存在或已被删除");

        Map<String, Object> detail = new LinkedHashMap<>(matches.get(0));
        long policyStart = ((Number) detail.get("p_starttime")).longValue();
        long policyEnd = ((Number) detail.get("p_endtime")).longValue();
        long streamStart = detail.get("stream_starttime") instanceof Number ? ((Number) detail.get("stream_starttime")).longValue() : 0L;
        long streamEnd = detail.get("stream_endtime") instanceof Number ? ((Number) detail.get("stream_endtime")).longValue() : 0L;
        detail.put("effective_starttime", streamStart == 0 ? policyStart : Math.max(policyStart, streamStart));
        detail.put("effective_endtime", streamEnd == 0 ? policyEnd : Math.min(policyEnd, streamEnd));
        detail.put("authorization_rules", federation
                ? List.of("查询必须位于所有参与策略的公共授权区间", "至少选择两个联邦策略", "控制器签发联邦查询令牌", "仅返回联合统计结果")
                : List.of("消费者必须与策略授权对象一致", "查询时间不得超出授权区间", "查询粒度不得小于策略倍数", "仅返回授权范围内的聚合结果"));
        detail.put("calculation_path", federation
                ? List.of("匹配同一消费者的联邦策略", "计算授权时间交集", "协商联邦令牌", "分别聚合各数据流密文", "合并统计量并返回结果")
                : List.of("校验 Owner、Consumer 与数据流", "校验时间和粒度", "派生授权时间片节点", "执行密文块聚合", "抵消边界钥片并恢复统计量"));
        detail.put("csv_fingerprint", anchors.latestUploadFingerprint(
                Long.parseLong(String.valueOf(detail.get("stream_id")))));

        if (federation) {
            String consumer = String.valueOf(detail.get("consumer_name"));
            List<Map<String, Object>> peers = rows(
                    "SELECT CAST(pm.policy_id AS CHAR) policy_id,CAST(pm.stream_id AS CHAR) stream_id," +
                            "pm.owner_name,pm.consumer_name,pm.p_starttime,pm.p_endtime,pm.mingranularity granularity," +
                            "s.name stream_name,s.description stream_description " +
                            "FROM policy_mpc pm LEFT JOIN stream s ON s.id=pm.stream_id " +
                            "WHERE pm.consumer_name=? AND pm.p_starttime < ? AND pm.p_endtime > ? ORDER BY pm.p_starttime",
                    consumer, policyEnd, policyStart);
            long commonStart = Long.MIN_VALUE;
            long commonEnd = Long.MAX_VALUE;
            for (Map<String, Object> peer : peers) {
                commonStart = Math.max(commonStart, ((Number) peer.get("p_starttime")).longValue());
                commonEnd = Math.min(commonEnd, ((Number) peer.get("p_endtime")).longValue());
            }
            detail.put("federation_group", peers);
            detail.put("common_starttime", commonStart == Long.MIN_VALUE ? null : commonStart);
            detail.put("common_endtime", commonEnd == Long.MAX_VALUE ? null : commonEnd);
            detail.put("common_range_valid", peers.size() >= 2 && commonStart < commonEnd);
        }
        detail.put("blockchain_anchor", anchors.findByBusiness(
                federation ? "FEDERATION_POLICY" : "POLICY", id));
        recordAdmin(admin, "POLICY_VIEW", (federation ? "federation" : "normal") + "-policy", id,
                "viewed full policy definition and authorization path");
        return ApiResponse.ok(detail);
    }

    @DeleteMapping("/policies/{type}/{id}")
    public ApiResponse<Void> deletePolicy(@RequestHeader("X-Token") String token, @PathVariable String type, @PathVariable String id) {
        UserSession admin = sessions.requireAdmin(token);
        String table = "federation".equals(type) ? "policy_mpc" : "policy";
        update("DELETE FROM " + table + " WHERE policy_id=?", id);
        recordAdmin(admin, "POLICY_DELETE", type + "-policy", id, "table=" + table);
        return ApiResponse.ok();
    }

    @GetMapping("/audit")
    public ApiResponse<List<Map<String, Object>>> audit(@RequestHeader("X-Token") String token,
                                                        @RequestParam(defaultValue="300") int limit,
                                                        @RequestParam(required=false) String action,
                                                        @RequestParam(required=false) String targetId) {
        sessions.requireAdmin(token); return ApiResponse.ok(audit.list(limit, action, targetId));
    }

    @GetMapping("/streams/{id}/trace")
    public ApiResponse<Map<String, Object>> trace(@RequestHeader("X-Token") String token, @PathVariable long id) {
        UserSession admin = sessions.requireAdmin(token); Map<String, Object> result = traces.trace(id);
        recordAdmin(admin, "STREAM_TRACE", "stream", String.valueOf(id), "recomputed anomaly and aggregation trace");
        return ApiResponse.ok(result);
    }

    @GetMapping("/anchors")
    public ApiResponse<List<Map<String, Object>>> anchors(@RequestHeader("X-Token") String token,
                                                           @RequestParam(defaultValue = "500") int limit,
                                                           @RequestParam(required = false) String status,
                                                           @RequestParam(required = false) String type) {
        sessions.requireAdmin(token);
        return ApiResponse.ok(anchors.list(limit, status, type));
    }

    @GetMapping("/anchors/{id}")
    public ApiResponse<Map<String, Object>> anchorDetail(@RequestHeader("X-Token") String token,
                                                          @PathVariable long id) {
        UserSession admin = sessions.requireAdmin(token);
        Map<String, Object> result = anchors.detail(id);
        recordAdmin(admin, "ANCHOR_VIEW", "blockchain-anchor", String.valueOf(id), "viewed payload and verification result");
        return ApiResponse.ok(result);
    }

    @PostMapping("/anchors/{id}/retry")
    public ApiResponse<Void> retryAnchor(@RequestHeader("X-Token") String token, @PathVariable long id) {
        UserSession admin = sessions.requireAdmin(token);
        anchors.retry(id);
        recordAdmin(admin, "ANCHOR_RETRY", "blockchain-anchor", String.valueOf(id), "manual retry requested");
        return ApiResponse.ok();
    }

    private void assertNotAdmin(String number) {
        long count = scalar("SELECT COUNT(*) FROM custom WHERE number=? AND identity=?", number, AdminAuditService.ADMIN_IDENTITY);
        if (count > 0) throw new GatewayException("唯一超级管理员不能被禁用或删除");
    }

    private void deleteStreamRecords(String id) {
        try (Connection c = Connect.getConnection()) {
            c.setAutoCommit(false);
            try {
                for (String table : List.of("history", "policy", "policy_mpc", "stream_producer", "owner_stream"))
                    execute(c, "DELETE FROM " + table + " WHERE " + ("history".equals(table) ? "streamid" : "stream_id") + "=?", id);
                execute(c, "DELETE FROM stream WHERE id=?", id); c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
        } catch (Exception e) { throw new GatewayException("删除数据流失败，已回滚：" + e.getMessage(), e); }
    }

    private void recordAdmin(UserSession admin, String action, String type, String id, String detail) {
        audit.record(UUID.randomUUID().toString().replace("-", ""), admin, action, null, "/api/admin", type, id, detail, true, 0, null);
    }
    private long scalar(String sql, Object... params) {
        try (Connection c=Connect.getConnection(); PreparedStatement s=c.prepareStatement(sql)) { bind(s,params); try(ResultSet r=s.executeQuery()){ return r.next()?r.getLong(1):0; } }
        catch(SQLException e){ throw new GatewayException("统计失败："+e.getMessage(),e); }
    }
    private List<Map<String,Object>> rows(String sql,Object...params){
        try(Connection c=Connect.getConnection();PreparedStatement s=c.prepareStatement(sql)){bind(s,params);try(ResultSet r=s.executeQuery()){List<Map<String,Object>> out=new ArrayList<>();ResultSetMetaData m=r.getMetaData();while(r.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=m.getColumnCount();i++)row.put(m.getColumnLabel(i),r.getObject(i));out.add(row);}return out;}}
        catch(SQLException e){throw new GatewayException("读取管理数据失败："+e.getMessage(),e);}
    }
    private int update(String sql,Object...params){try(Connection c=Connect.getConnection();PreparedStatement s=c.prepareStatement(sql)){bind(s,params);return s.executeUpdate();}catch(SQLException e){throw new GatewayException("管理操作失败："+e.getMessage(),e);}}
    private static int execute(Connection c,String sql,Object...params)throws SQLException{try(PreparedStatement s=c.prepareStatement(sql)){bind(s,params);return s.executeUpdate();}}
    private static void bind(PreparedStatement s,Object...params)throws SQLException{for(int i=0;i<params.length;i++)s.setObject(i+1,params[i]);}
}
