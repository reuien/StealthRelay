package com.example.gateway.controller;

import sqlConnect.FrontEndSQL;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> resp = new HashMap<>();
        try {
            FrontEndSQL sql = new FrontEndSQL();
            List<String> consumers = sql.searchCustom();
            resp.put("status", "UP");
            resp.put("db", "connected");
            resp.put("consumerCount", consumers == null ? 0 : consumers.size());
            resp.put("consumers", consumers);
        } catch (Throwable e) {
            resp.put("status", "DOWN");
            resp.put("db", "error");
            resp.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return resp;
    }
}
