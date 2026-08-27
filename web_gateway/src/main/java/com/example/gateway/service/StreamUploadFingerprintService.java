package com.example.gateway.service;

import com.example.gateway.support.GatewayException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import sqlConnect.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StreamUploadFingerprintService {
    @PostConstruct
    public void initialize() {
        try (Connection connection = Connect.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS stream_upload_fingerprint (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "stream_id BIGINT NOT NULL,owner_id VARCHAR(64) NOT NULL,owner_name VARCHAR(255) NOT NULL," +
                    "file_name VARCHAR(512) NOT NULL,file_sha256 CHAR(64) NOT NULL,size_bytes BIGINT NOT NULL," +
                    "total_rows INT NOT NULL,valid_rows INT NOT NULL,uploaded_at BIGINT NOT NULL," +
                    "INDEX idx_upload_fingerprint_stream(stream_id,uploaded_at)," +
                    "UNIQUE KEY uk_upload_fingerprint(stream_id,file_sha256)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (SQLException e) {
            throw new IllegalStateException("初始化CSV指纹表失败：" + e.getMessage(), e);
        }
    }

    public void recordCsv(long streamId, String ownerId, String ownerName, String fileName,
                          String sha256, long sizeBytes, int totalRows, int validRows, long uploadedAt) {
        String sql = "INSERT INTO stream_upload_fingerprint(stream_id,owner_id,owner_name,file_name,file_sha256," +
                "size_bytes,total_rows,valid_rows,uploaded_at) VALUES(?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE owner_id=VALUES(owner_id),owner_name=VALUES(owner_name)," +
                "file_name=VALUES(file_name),size_bytes=VALUES(size_bytes),total_rows=VALUES(total_rows)," +
                "valid_rows=VALUES(valid_rows),uploaded_at=VALUES(uploaded_at)";
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, streamId);
            statement.setString(2, ownerId);
            statement.setString(3, ownerName);
            statement.setString(4, fileName);
            statement.setString(5, sha256);
            statement.setLong(6, sizeBytes);
            statement.setInt(7, totalRows);
            statement.setInt(8, validRows);
            statement.setLong(9, uploadedAt);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new GatewayException("保存CSV数字指纹失败：" + e.getMessage(), e);
        }
    }

    public Map<String, Object> latestForStream(long streamId) {
        String sql = "SELECT CAST(id AS CHAR) upload_id,CAST(stream_id AS CHAR) stream_id,file_name,file_sha256," +
                "size_bytes,total_rows,valid_rows,uploaded_at FROM stream_upload_fingerprint " +
                "WHERE stream_id=? ORDER BY uploaded_at DESC,id DESC LIMIT 1";
        try (Connection connection = Connect.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, streamId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return null;
                ResultSetMetaData meta = rs.getMetaData();
                Map<String, Object> result = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) result.put(meta.getColumnLabel(i), rs.getObject(i));
                return result;
            }
        } catch (SQLException e) {
            throw new GatewayException("读取CSV数字指纹失败：" + e.getMessage(), e);
        }
    }
}
