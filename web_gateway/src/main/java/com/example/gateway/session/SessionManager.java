package com.example.gateway.session;

import com.example.gateway.support.GatewayException;
import org.springframework.stereotype.Component;
import usrs.DataConsumer;
import usrs.DataOwnerClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private static final String KEYSTORE_PASSWORD = "usrTestCryptPassword";

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSession create(String number, String usrName, String role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        UserSession session = new UserSession(token, number, usrName, role);
        sessions.put(token, session);
        return session;
    }

    public UserSession require(String token) {
        if (token == null || token.isEmpty()) {
            throw new GatewayException("缺少登录令牌，请重新登录");
        }
        UserSession s = sessions.get(token);
        if (s == null) {
            throw new GatewayException("登录已失效，请重新登录");
        }
        s.touch();
        return s;
    }

    public UserSession requireOwner(String token) {
        UserSession session = require(token);
        if (!"owner".equals(session.getRole())) {
            throw new GatewayException("当前接口仅允许数据拥有者访问");
        }
        return session;
    }

    public UserSession requireConsumer(String token) {
        UserSession session = require(token);
        if (!"consumer".equals(session.getRole())) {
            throw new GatewayException("当前接口仅允许数据消费者访问");
        }
        return session;
    }

    public UserSession requireAdmin(String token) {
        UserSession session = require(token);
        if (!"admin".equals(session.getRole())) {
            throw new GatewayException("当前接口仅允许超级管理员访问");
        }
        return session;
    }

    public UserSession find(String token) {
        return token == null ? null : sessions.get(token);
    }

    public void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public synchronized DataOwnerClient ownerClient(UserSession session) {
        if (session.getOwnerClient() != null) {
            return session.getOwnerClient();
        }
        try {
            DataOwnerClient doc = new DataOwnerClient(session.getNumber(), session.getUsrName(), KEYSTORE_PASSWORD);
            session.setOwnerClient(doc);
            return doc;
        } catch (Throwable e) {
            throw new GatewayException("无法连接数据服务（请确认 1101/1102/1234 后端已启动）：" + e.getMessage(), e);
        }
    }

    public synchronized DataConsumer consumer(UserSession session) {
        if (session.getConsumer() != null) {
            return session.getConsumer();
        }
        try {
            DataConsumer dc = new DataConsumer(session.getUsrName());
            session.setConsumer(dc);
            return dc;
        } catch (Throwable e) {
            throw new GatewayException("无法连接数据服务（请确认 1101/1102 后端已启动）：" + e.getMessage(), e);
        }
    }
}
