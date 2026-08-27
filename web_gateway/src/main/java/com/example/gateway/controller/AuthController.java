package com.example.gateway.controller;

import com.example.gateway.session.SessionManager;
import com.example.gateway.session.UserSession;
import com.example.gateway.support.ApiResponse;
import com.example.gateway.support.GatewayException;
import sqlConnect.FrontEndSQL;
import Item.Custom;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.example.gateway.service.AdminAuditService;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final SessionManager sessionManager;
    private final FrontEndSQL sql = new FrontEndSQL();
    private final Random rand = new Random();
    private final AdminAuditService adminAuditService;

    public AuthController(SessionManager sessionManager, AdminAuditService adminAuditService) {
        this.sessionManager = sessionManager;
        this.adminAuditService = adminAuditService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String number = trim(body.get("number"));
        String password = body.get("password");
        String role = trim(body.get("role"));
        if (number.isEmpty() || password == null) {
            throw new GatewayException("账号或密码不能为空");
        }

        boolean ok;
        String usrName;
        String effectiveRole = role.toLowerCase();
        if ("owner".equalsIgnoreCase(role)) {
            // 超级管理员是交通数据节点的特殊分类。登录入口与普通 Owner 相同，
            // 仅由服务端依据唯一账号身份提升为 admin 会话。
            Map<String, String> admin = adminAuditService.authenticateAdmin(number, password);
            if (admin != null) {
                ok = true;
                usrName = admin.get("usrName");
                effectiveRole = "admin";
            } else {
                ok = sql.Owner_Login(number, password);
                usrName = sql.searchName(number);
                effectiveRole = "owner";
            }
        } else if ("consumer".equalsIgnoreCase(role)) {
            ok = sql.Consumer_Login(number, password);
            usrName = sql.searchName(number);
            effectiveRole = "consumer";
        } else {
            throw new GatewayException("role 必须是 owner 或 consumer");
        }
        if (!ok) {
            throw new GatewayException("账号、密码或角色不匹配");
        }
        if (adminAuditService.isDisabled(number)) {
            throw new GatewayException("该账号已被超级管理员禁用");
        }

        UserSession session = sessionManager.create(number, usrName, effectiveRole);

        Map<String, Object> data = new HashMap<>();
        data.put("token", session.getToken());
        data.put("number", number);
        data.put("usrName", usrName);
        data.put("role", session.getRole());
        return ApiResponse.ok(data);
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String usrName = trim(body.get("usrName"));
        String password = body.get("password");
        String role = trim(body.get("role"));
        if (usrName.isEmpty() || password == null || password.isEmpty()) {
            throw new GatewayException("用户名或密码不能为空");
        }
        String identity;
        if ("owner".equalsIgnoreCase(role)) {
            identity = "拥有者";
        } else if ("consumer".equalsIgnoreCase(role)) {
            identity = "消费者";
        } else {
            throw new GatewayException("role 必须是 owner 或 consumer");
        }
        if (sql.NameIsExisted(usrName, identity)) {
            throw new GatewayException("该用户名已被注册");
        }

        String number = null;
        for (int i = 0; i < 50; i++) {
            String candidate = String.valueOf(1000 + rand.nextInt(9000));
            if (!sql.NumberIsExisted(candidate, identity)) {
                number = candidate;
                break;
            }
        }
        if (number == null) {
            throw new GatewayException("账号生成失败，请重试");
        }

        Custom c = new Custom(usrName, number, password, identity);
        sql.Owner_Regist(c);

        Map<String, Object> data = new HashMap<>();
        data.put("number", number);
        data.put("usrName", usrName);
        data.put("role", role.toLowerCase());
        return ApiResponse.ok(data);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "X-Token", required = false) String token) {
        sessionManager.remove(token);
        return ApiResponse.ok();
    }

    @GetMapping("/consumers")
    public ApiResponse<List<String>> consumers(@RequestHeader("X-Token") String token) {
        sessionManager.requireOwner(token);
        return ApiResponse.ok(sql.searchCustom());
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
