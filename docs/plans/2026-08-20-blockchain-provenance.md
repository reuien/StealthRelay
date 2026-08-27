# Blockchain Provenance Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为策略、联邦策略和计算异常记录实现可靠异步区块链存证，并在超级管理员页面提供查看、验证和重试能力。

**Architecture:** MySQL 触发器创建 outbox 存证任务，Spring 定时执行器抢占并处理任务。规范化 JSON 使用 SHA-256 生成公开指纹，Web3j 将摘要写入以太坊交易 input；管理员 API 和 Vue 页面展示数据库载荷、摘要、交易回执与校验结果。

**Tech Stack:** Java 17、Spring Boot 3、JDBC、MySQL 8、Web3j、Vue 3、Element Plus、SHA-256。

---

### Task 1: 数据库与摘要基础

**Files:**
- Create: `web_gateway/src/main/java/com/example/gateway/service/BlockchainAnchorService.java`
- Modify: `data/sys_plus.sql`
- Test: `web_gateway/src/test/java/com/example/gateway/service/CanonicalHashTest.java`

1. 添加失败测试，验证字段顺序固定、相同业务内容产生相同 SHA-256、字段变化会改变摘要。
2. 新增 `blockchain_anchor`、`computation_trace` 和唯一索引。
3. 新增策略表插入触发器，可靠创建待处理任务。
4. 实现规范化载荷与 SHA-256。
5. 运行单元测试与数据库结构检查。

### Task 2: 可靠异步上链执行器

**Files:**
- Create: `web_gateway/src/main/java/com/example/gateway/service/EthereumAnchorClient.java`
- Modify: `web_gateway/src/main/java/com/example/gateway/service/BlockchainAnchorService.java`
- Modify: `web_gateway/src/main/java/com/example/gateway/WebGatewayApplication.java`
- Modify: `web_gateway/src/main/resources/application.properties`

1. 添加环境变量配置并移除任何默认私钥。
2. 使用条件更新抢占任务，恢复超时任务并限制重试次数。
3. 使用零金额自交易把摘要放入 input，校验交易回执状态。
4. 保存交易哈希、区块高度、链 ID、发送地址和确认时间。
5. 验证未配置链时业务保持可用且任务明确显示待配置。

### Task 3: 策略与计算溯源接入

**Files:**
- Modify: `web_gateway/src/main/java/com/example/gateway/service/AdminTraceService.java`
- Modify: `web_gateway/src/main/java/com/example/gateway/controller/AdminController.java`

1. 查询现有普通策略与联邦策略并回填待存证任务。
2. 数据流异常分析完成后写入计算追踪记录和存证任务。
3. 策略详情附带关联存证状态。
4. 增加存证列表、详情、验证和手动重试接口。
5. 验证所有接口都要求超级管理员会话。

### Task 4: 管理员页面

**Files:**
- Modify: `web_frontend/src/api/index.js`
- Modify: `web_frontend/src/views/admin/Overview.vue`

1. 增加存证 API 客户端。
2. 增加存证统计卡和“区块链存证”页签。
3. 展示状态、摘要、交易、区块高度、重试和错误信息。
4. 增加详情抽屉、校验和手动重试。
5. 构建前端确认无模板或类型错误。

### Task 5: 清理旧秘密与验证

**Files:**
- Modify: `privacy_policy_controller/src/main/java/blk/StoreInterface.java`
- Modify: `traffic_access_core/src/main/java/blk/StoreInterface.java`
- Modify: `config/database.properties.example`（如不存在则创建）
- Modify: `项目要点总结.md`

1. 删除源码硬编码区块链私钥和 RPC 项目标识。
2. 统一改为环境变量配置并对缺失配置安全失败。
3. 构建所有受影响 Maven 模块与 Vue 前端。
4. 在 `attempt` 检查任务幂等、状态和摘要。
5. 重启服务并执行健康、管理员授权及存证 API 验证。

