package com.example.gateway.service;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CanonicalHashTest {
    @Test
    void mapOrderDoesNotChangeCanonicalHash() {
        Map<String, Object> first = new LinkedHashMap<>(); first.put("owner", "A"); first.put("stream", 42);
        Map<String, Object> second = new LinkedHashMap<>(); second.put("stream", 42); second.put("owner", "A");
        assertEquals(BlockchainAnchorService.canonicalJson(first), BlockchainAnchorService.canonicalJson(second));
        assertEquals(BlockchainAnchorService.sha256(BlockchainAnchorService.canonicalJson(first)),
                BlockchainAnchorService.sha256(BlockchainAnchorService.canonicalJson(second)));
    }

    @Test
    void changedFieldChangesHash() {
        assertNotEquals(BlockchainAnchorService.sha256("{\"value\":1}"),
                BlockchainAnchorService.sha256("{\"value\":2}"));
    }

    @Test
    void federationCredentialBindsConsumerPoliciesStreamsAndTimeRange() {
        Map<String, Object> credential = new LinkedHashMap<>();
        credential.put("consumer", "consumer-11");
        credential.put("policyIds", List.of(101L, 102L));
        credential.put("streamIds", List.of(201L, 202L));
        credential.put("startTime", 1_000L);
        credential.put("endTime", 2_000L);
        String original = BlockchainAnchorService.sha256(BlockchainAnchorService.canonicalJson(credential));
        credential.put("endTime", 2_001L);
        String changed = BlockchainAnchorService.sha256(BlockchainAnchorService.canonicalJson(credential));
        assertNotEquals(original, changed);
    }
}
