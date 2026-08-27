package com.example.gateway.service;

import com.example.gateway.session.UserSession;
import com.example.gateway.support.GatewayException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sqlConnect.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAuditService {
    public static final String ADMIN_IDENTITY = "超级管理员";

    @Value("${gateway.admin.number}") private String adminNumber;
    @Value("${gateway.admin.name}") private String adminName;
    @Value("${gateway.admin.password}") private String adminPassword;

    @PostConstruct
    public void initialize() {
        try (Connection connection = Connect.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS admin_audit_log (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "trace_id VARCHAR(64) NOT NULL, event_time BIGINT NOT NULL," +
                    "actor_number VARCHAR(255), actor_name VARCHAR(255), actor_role VARCHAR(32)," +
                    "action VARCHAR(64) NOT NULL, method VARCHAR(16), request_path VARCHAR(512)," +
                    "target_type VARCHAR(64), target_id VARCHAR(255), detail TEXT," +
                    "success TINYINT(1) NOT NULL, duration_ms BIGINT NOT NULL DEFAULT 0," +
                    "source_ip VARCHAR(128), INDEX idx_audit_time(event_time)," +
                    "INDEX idx_audit_trace(trace_id), INDEX idx_audit_target(target_type, target_id))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS user_account_status (" +
                    "user_number VARCHAR(255) NOT NULL PRIMARY KEY, disabled TINYINT(1) NOT NULL DEFAULT 0," +
                    "updated_at BIGINT NOT NULL, updated_by VARCHAR(255), reason VARCHAR(500))");
            bootstrapSingleAdmin(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("初始化超级管理员与审计表失败：" + e.getMessage(), e);
        }
    }

    private void bootstrapSingleAdmin(Connection connection) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM custom WHERE identity = ? AND number <> ?")) {
            delete.setString(1, ADMIN_IDENTITY);
            delete.setString(2, adminNumber);
            delete.executeUpdate();
        }
        try (PreparedStatement upsert = connection.prepareStatement(
                "INSERT INTO custom(number, usr_name, password, identity) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE usr_name=VALUES(usr_name), password=VALUES(password), identity=VALUES(identity)")) {
            upsert.setString(1, adminNumber);
            upsert.setString(2, adminName);
            upsert.setString(3, adminPassword);
            upsert.setString(4, ADMIN_IDENTITY);
            upsert.executeUpdate();
        }
    }

    public Map<String, String> authenticateAdmin(String number, String password) {
        String sql = "SELECT number, usr_name FROM custom WHERE number=? AND password=? AND identity=?";
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, number);
            statement.setString(2, password);
            statement.setString(3, ADMIN_IDENTITY);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return null;
                return Map.of("number", rs.getString("number"), "usrName", rs.getString("usr_name"));
            }
        } catch (SQLException e) {
            throw new GatewayException("管理员认证失败：" + e.getMessage(), e);
        }
    }

    public boolean isDisabled(String number) {
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT disabled FROM user_account_status WHERE user_number=?")) {
            statement.setString(1, number);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new GatewayException("读取账号状态失败：" + e.getMessage(), e);
        }
    }

    public void record(String traceId, UserSession actor, String action, String method, String path,
                       String targetType, String targetId, String detail, boolean success,
                       long durationMs, String sourceIp) {
        String sql = "INSERT INTO admin_audit_log(trace_id,event_time,actor_number,actor_name,actor_role," +
                "action,method,request_path,target_type,target_id,detail,success,duration_ms,source_ip) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, traceId);
            statement.setLong(2, System.currentTimeMillis());
            statement.setString(3, actor == null ? null : actor.getNumber());
            statement.setString(4, actor == null ? null : actor.getUsrName());
            statement.setString(5, actor == null ? "anonymous" : actor.getRole());
            statement.setString(6, action);
            statement.setString(7, method);
            statement.setString(8, path);
            statement.setString(9, targetType);
            statement.setString(10, targetId);
            statement.setString(11, truncate(detail, 4000));
            statement.setBoolean(12, success);
            statement.setLong(13, durationMs);
            statement.setString(14, sourceIp);
            statement.executeUpdate();
        } catch (SQLException ignored) {
            // 审计失败不能遮蔽原始业务响应；健康页可通过日志列表发现缺口。
        }
    }

    public List<Map<String, Object>> list(int limit, String action, String targetId) {
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        StringBuilder sql = new StringBuilder("SELECT * FROM admin_audit_log WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (action != null && !action.isBlank()) { sql.append(" AND action=?"); params.add(action.trim()); }
        if (targetId != null && !targetId.isBlank()) { sql.append(" AND target_id=?"); params.add(targetId.trim()); }
        sql.append(" ORDER BY id DESC LIMIT ").append(safeLimit);
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) statement.setString(i + 1, params.get(i));
            try (ResultSet rs = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id")); row.put("traceId", rs.getString("trace_id"));
                    row.put("eventTime", rs.getLong("event_time")); row.put("actorNumber", rs.getString("actor_number"));
                    row.put("actorName", rs.getString("actor_name")); row.put("actorRole", rs.getString("actor_role"));
                    row.put("action", rs.getString("action")); row.put("method", rs.getString("method"));
                    row.put("path", rs.getString("request_path")); row.put("targetType", rs.getString("target_type"));
                    row.put("targetId", rs.getString("target_id")); row.put("detail", rs.getString("detail"));
                    row.put("success", rs.getBoolean("success")); row.put("durationMs", rs.getLong("duration_ms"));
                    row.put("sourceIp", rs.getString("source_ip")); rows.add(row);
                }
                return rows;
            }
        } catch (SQLException e) {
            throw new GatewayException("读取审计日志失败：" + e.getMessage(), e);
        }
    }

    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) return value;
        return value.substring(0, max) + "…";
    }
}
