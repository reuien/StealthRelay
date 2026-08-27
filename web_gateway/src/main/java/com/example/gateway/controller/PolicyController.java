package com.example.gateway.controller;

import com.example.gateway.session.SessionManager;
import com.example.gateway.session.UserSession;
import com.example.gateway.support.ApiResponse;
import com.example.gateway.support.GatewayException;
import sqlConnect.FrontEndSQL;
import Item.PrivacyPolicy;
import usrs.DataOwnerClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PolicyController {

    private final SessionManager sessionManager;
    private final FrontEndSQL sql = new FrontEndSQL();

    public PolicyController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @PostMapping("/policies")
    public ApiResponse<Map<String, Object>> create(@RequestHeader("X-Token") String token,
                                                    @RequestBody Map<String, Object> body) {
        UserSession session = sessionManager.requireOwner(token);
        String consumerName = str(body.get("consumerName"));
        long streamId = lng(body.get("streamId"));
        long startMs = lng(body.get("startTime"));
        long endMs = lng(body.get("endTime"));
        long minGranularity = lng(body.get("minGranularity"));
        if (minGranularity == 0) {
            minGranularity = lng(body.get("minGranularityMillis"));
        }
        if (minGranularity >= 1000) {
            throw new GatewayException("策略最小粒度应传倍数（如 1、2、5），不是毫秒值");
        }
        String policyName = str(body.get("policyName"));

        if (consumerName.isEmpty()) {
            throw new GatewayException("请选择被授权的消费者");
        }
        if (streamId == 0) {
            throw new GatewayException("请选择数据流");
        }
        if (startMs <= 0 || endMs <= 0 || startMs >= endMs) {
            throw new GatewayException("请提供正确的策略时间范围（起 < 止）");
        }
        if (policyName.isEmpty()) {
            throw new GatewayException("请输入策略名称");
        }
        assertOwnedStream(session, streamId);

        DataOwnerClient doc = sessionManager.ownerClient(session);
        long policyId;
        try {
            streamHandling.PrivacyPolicy pp = doc.createPrivacyPolicy(
                    consumerName, streamId, new Date(startMs), new Date(endMs), (int) minGranularity);
            policyId = pp.getPrivacyPolicyId();
        } catch (Exception e) {
            throw new GatewayException("创建策略失败（请确认后端 1101/1102 已启动）：" + e.getMessage(), e);
        }

        PrivacyPolicy item = new PrivacyPolicy();
        item.setUsrName(session.getUsrName());
        item.setCustname(consumerName);
        item.setPrivacyPolicyId(policyId);
        item.setStreamID(streamId);
        item.setStartTime(new Date(startMs));
        item.setEndTime(new Date(endMs));
        item.setMinGranularity(minGranularity);
        item.setPolicyName(policyName);
        sql.insertPolicy(item);

        Map<String, Object> data = new HashMap<>();
        data.put("policyId", String.valueOf(policyId));
        data.put("policyName", policyName);
        return ApiResponse.ok(data);
    }

    @GetMapping("/policies")
    public ApiResponse<List<Map<String, Object>>> list(@RequestHeader("X-Token") String token,
                                                       @RequestParam(required = false) String consumer) {
        UserSession session = sessionManager.requireConsumer(token);
        assertSameConsumer(session, consumer);
        List<Item.PrivacyPolicy> policies = sql.searchPolicy(session.getUsrName());
        return ApiResponse.ok(toPolicyMaps(policies));
    }

    @GetMapping("/owner/policies")
    public ApiResponse<List<Map<String, Object>>> ownerList(@RequestHeader("X-Token") String token) {
        UserSession session = sessionManager.requireOwner(token);
        List<Item.PrivacyPolicy> policies = sql.searchPolicyByOwner(session.getUsrName());
        return ApiResponse.ok(toPolicyMaps(policies));
    }

    @DeleteMapping("/owner/policies/{policyId}")
    public ApiResponse<Map<String, Object>> delete(@RequestHeader("X-Token") String token,
                                                   @PathVariable("policyId") String policyIdText) {
        UserSession session = sessionManager.requireOwner(token);
        long policyId = lng(policyIdText);
        if (policyId == 0) {
            throw new GatewayException("非法的策略 ID");
        }
        Item.PrivacyPolicy policy = requireOwnedPolicy(session, policyId);
        sql.deletePolicy(policyId);
        Map<String, Object> data = new HashMap<>();
        data.put("policyId", String.valueOf(policyId));
        data.put("deleted", true);
        return ApiResponse.ok(data);
    }

    private List<Map<String, Object>> toPolicyMaps(List<Item.PrivacyPolicy> policies) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (policies != null) {
            for (Item.PrivacyPolicy p : policies) {
                Map<String, Object> m = new HashMap<>();
                m.put("policyId", String.valueOf(p.getPrivacyPolicyId()));
                m.put("policyName", p.getPolicyName());
                m.put("ownerName", p.getUsrName());
                m.put("consumerName", p.getCustname());
                m.put("streamId", String.valueOf(p.getStreamID()));
                String nameId = sql.searchStream_Name_ID(p.getStreamID()); // "name*id"
                m.put("streamName", nameId == null ? null : nameId.split("\\*")[0]);
                m.put("startTime", p.getStartTime() == null ? null : p.getStartTime().getTime());
                m.put("endTime", p.getEndTime() == null ? null : p.getEndTime().getTime());
                m.put("minGranularity", p.getMinGranularity());
                result.add(m);
            }
        }
        return result;
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
        throw new GatewayException("不能为其他拥有者的数据流制定策略");
    }

    private Item.PrivacyPolicy requireOwnedPolicy(UserSession session, long policyId) {
        List<Item.PrivacyPolicy> policies = sql.searchPolicyByOwner(session.getUsrName());
        if (policies != null) {
            for (Item.PrivacyPolicy policy : policies) {
                if (policy.getPrivacyPolicyId() == policyId) {
                    return policy;
                }
            }
        }
        throw new GatewayException("不能删除其他拥有者的策略");
    }

    private static void assertSameConsumer(UserSession session, String consumer) {
        if (consumer != null && !consumer.trim().isEmpty() && !session.getUsrName().equals(consumer.trim())) {
            throw new GatewayException("不能查看其他消费者的策略");
        }
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
