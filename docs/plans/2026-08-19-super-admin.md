# Super Admin Audit and Traceability Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add one powerful super administrator who can manage every user resource and trace abnormal traffic points through source, policy, audit, and aggregation details.

**Architecture:** Add a gateway-owned admin/audit layer backed by MySQL and reuse existing stream CSV storage for anomaly analysis. Add a dedicated Vue administration console; keep the legacy cryptographic services unchanged and label reconstructed cryptographic explanations separately from observed runtime facts.

**Tech Stack:** Java 17, Spring Boot 3, JDBC/MySQL, Vue 3, Element Plus, ECharts

---

### Task 1: Administrator identity and audit storage

**Files:**
- Modify: `web_gateway/src/main/resources/application.properties`
- Modify: `web_gateway/src/main/java/com/example/gateway/controller/AuthController.java`
- Modify: `web_gateway/src/main/java/com/example/gateway/session/SessionManager.java`
- Create: `web_gateway/src/main/java/com/example/gateway/service/AdminAuditService.java`
- Create: `web_gateway/src/main/java/com/example/gateway/support/AuditInterceptor.java`

Add the unique `超级管理员` bootstrap account, admin session guard, disabled-account checks, audit table initialization, and request tracing. Verify with `mvn -pl web_gateway -am -DskipTests package`.

### Task 2: Global administration and trace APIs

**Files:**
- Create: `web_gateway/src/main/java/com/example/gateway/controller/AdminController.java`
- Create: `web_gateway/src/main/java/com/example/gateway/service/AdminTraceService.java`

Implement summary, users, streams, policies, audit logs, user status changes, destructive resource management, anomaly detection, provenance, and aggregation-step endpoints. Verify controller compilation and role guards.

### Task 3: Administration console

**Files:**
- Modify: `web_frontend/src/api/index.js`
- Modify: `web_frontend/src/router/index.js`
- Modify: `web_frontend/src/layouts/MainLayout.vue`
- Modify: `web_frontend/src/views/Login.vue`
- Create: `web_frontend/src/views/admin/Overview.vue`
- Create: `web_frontend/src/views/admin/Streams.vue`
- Create: `web_frontend/src/views/admin/Audit.vue`

Add administrator login, navigation, resource management, anomaly timeline, provenance panel, and aggregation process visualization. Require confirmation for destructive operations. Verify with `npm --prefix web_frontend run build`.

### Task 4: Database bootstrap and end-to-end verification

**Files:**
- Modify: `data/sys_plus.sql`
- Modify: `项目要点总结.md` if present in the repository handoff location

Document optional schema bootstrap, build backend and frontend, and inspect the rendered admin routes. Confirm that no API can delete audit records and that cryptographic process entries are labeled `observed` or `derived`.
