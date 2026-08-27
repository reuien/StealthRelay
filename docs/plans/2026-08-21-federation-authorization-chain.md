# Federation Authorization and Query Credential Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make owner-side federation policies visible/manageable and ensure only an authorized, successfully executed federation query creates a blockchain credential.

**Architecture:** Keep federation policy definitions in `policy_mpc`, but stop treating their creation as a blockchain transaction. Add an owner-scoped read/delete API and a `federation_query_execution` audit record; enqueue the latter as `FEDERATION_QUERY_EXECUTION` only after token issuance and analytics both succeed.

**Tech Stack:** Spring Boot, JDBC/MySQL, Vue 3, Element Plus, local Ethereum/Anvil anchor worker.

---

### Task 1: Correct federation anchor lifecycle

**Files:**
- Modify: `web_gateway/src/main/java/com/example/gateway/service/BlockchainAnchorService.java`

1. Stop creating/backfilling `FEDERATION_POLICY` anchors.
2. Create `federation_query_execution` storage.
3. Add a transactional recorder that stores the execution and queues `FEDERATION_QUERY_EXECUTION`.
4. Teach payload loading to canonicalize the execution credential.

### Task 2: Enforce authorization and expose owner federation policies

**Files:**
- Modify: `web_gateway/src/main/java/com/example/gateway/controller/TrafficQueryController.java`

1. Inject the anchor service.
2. Add owner-scoped federation list and delete routes.
3. Require every requested policy to belong to the logged-in consumer.
4. Fail closed if federation token issuance fails.
5. Record the credential only after successful analytics.

### Task 3: Display and manage owner federation policies

**Files:**
- Modify: `web_frontend/src/api/index.js`
- Modify: `web_frontend/src/views/owner/Policies.vue`

1. Add owner federation list/delete API calls.
2. Merge normal and federation policies in the owner table.
3. Show policy type and route deletion to the correct endpoint.

### Task 4: Expose execution credential to consumers and verify

**Files:**
- Modify: `web_frontend/src/views/consumer/Federation.vue`
- Test: `web_gateway/src/test/java/com/example/gateway/service/CanonicalHashTest.java`

1. Display the returned execution credential/chain status.
2. Add a deterministic federation credential hash test.
3. Run backend tests/package and frontend production build.

