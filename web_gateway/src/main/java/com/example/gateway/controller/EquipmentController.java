package com.example.gateway.controller;

import com.example.gateway.session.SessionManager;
import com.example.gateway.session.UserSession;
import com.example.gateway.support.ApiResponse;
import com.example.gateway.support.GatewayException;
import sqlConnect.FrontEndSQL;
import Item.Equipment;
import usrs.DataOwnerClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EquipmentController {

    private final SessionManager sessionManager;
    private final FrontEndSQL sql = new FrontEndSQL();

    public EquipmentController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @GetMapping("/equipments")
    public ApiResponse<List<Map<String, Object>>> list(@RequestHeader("X-Token") String token,
                                                       @RequestParam(required = false) String ownerId) {
        UserSession session = sessionManager.requireOwner(token);
        assertSameOwner(session, ownerId);
        ownerId = session.getNumber();
        List<Equipment> eqs = sql.getEqResults(ownerId);
        List<Map<String, Object>> result = new ArrayList<>();
        if (eqs != null) {
            for (Equipment e : eqs) {
                Map<String, Object> m = new HashMap<>();
                m.put("eqId", e.getIdnum());
                m.put("ownerId", e.getOwner());
                m.put("name", e.getName());
                m.put("port", e.getPort());
                m.put("ip", e.getiP());
                result.add(m);
            }
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/equipments")
    public ApiResponse<Map<String, Object>> create(@RequestHeader("X-Token") String token,
                                                    @RequestBody Map<String, String> body) {
        UserSession session = sessionManager.requireOwner(token);
        String name = trim(body.get("name"));
        String port = trim(body.get("port"));
        String ip = trim(body.get("ip"));
        if (name.isEmpty() || port.isEmpty() || ip.isEmpty()) {
            throw new GatewayException("设备名称、端口、IP 不能为空");
        }
        int portNum;
        try {
            portNum = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new GatewayException("端口必须是数字");
        }

        String ownerId = session.getNumber();
        String lastId = sql.serarchLastID(ownerId);
        int newIdInt;
        try {
            newIdInt = Integer.parseInt(lastId) + 1;
        } catch (NumberFormatException e) {
            newIdInt = 1;
        }
        String newEqId = String.valueOf(newIdInt);

        DataOwnerClient doc = sessionManager.ownerClient(session);
        try {
            doc.registerProducer(newIdInt, name, ip, portNum);
        } catch (Exception e) {
            throw new GatewayException("注册生产者失败（请确认后端 producer 1234 已启动）：" + e.getMessage(), e);
        }

        sql.insertEqData(newEqId, ownerId, name, port, ip);

        Map<String, Object> data = new HashMap<>();
        data.put("eqId", newEqId);
        data.put("ownerId", ownerId);
        data.put("name", name);
        data.put("port", port);
        data.put("ip", ip);
        return ApiResponse.ok(data);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static void assertSameOwner(UserSession session, String ownerId) {
        if (ownerId != null && !ownerId.trim().isEmpty() && !session.getNumber().equals(ownerId.trim())) {
            throw new GatewayException("不能访问其他拥有者的设备");
        }
    }
}
