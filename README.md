# StealthRelay — 面向 V2V 数据的隐私计算增强安全加密系统

> 让车联网数据**可计算、可授权、可联合分析，但不直接暴露原始数据**。

本项目面向车联网 V2V（Vehicle-to-Vehicle）及路侧交通数据共享场景，解决车辆位置、速度、车流量、道路状态等时序数据在共享过程中的**隐私泄露**与**越权访问**问题。系统通过加密数据流、细粒度访问策略、查询令牌与联邦统计，使数据拥有者始终掌握数据控制权，数据消费者只能获得授权范围内的聚合结果。

## ✨ 核心特性

- **密文聚合**：服务器可直接在密文数据块上执行求和与合并，消费者再结合授权密钥得到统计结果，原始数据全程不落明文。
- **边界钥片抵消**：时间区间内部密钥片在聚合时相互抵消，聚合解密开销由 `O(N)` 降为接近 `O(1)`。
- **时间片授权索引树**：以流主密钥为根派生时间片密钥，授权时间范围只需下发少量树节点；消费者可推导授权区间内密钥，但无法反推根密钥或越权区间密钥。
- **多层数据安全**：数据块 `AES/GCM` 加密、统计量同态 MAC 校验、SHA-256 数据指纹与区块链异步存证，实现"数据可信 + 计算可信 + 事后可审计"。
- **联邦查询**：多个数据流策略取公共授权时间区间，令牌颁发失败则立即拒绝查询（失败关闭），成功后才生成链上执行凭证。
- **Web 全流程**：Vue 3 前端 + Spring Boot 网关，覆盖 Owner / Consumer / 超级管理员三类角色的完整主流程。

## 🏗️ 系统架构

系统采用「数据处理平面 + 隐私控制平面」双平面设计：

```text
Vue 3 前端（5173）
      ↓ /api（Vite 代理）
Spring Boot Web Gateway（8080）—— 认证、权限、REST API
      ↓
traffic_access_core（Java 核心业务/密码学模块）
      ↓
┌──────────────────────────┬──────────────────────────┐
│ 数据处理平面             │ 隐私控制平面             │
│ secure_stream_store 1101 │ privacy_policy_controller│
│ 密文存储/索引/Kafka 消费 │ 策略校验/令牌签发/密钥协商 │
│ traffic_stream_producer  │        1102              │
│ 数据生产/加密/上传 1234  │                          │
└──────────────────────────┴──────────────────────────┘
      ↓
MySQL + Kafka + 本地密钥库 + 上传文件 + Anvil 本地链（8545）
```

### 目录结构

| 目录 | 说明 |
| --- | --- |
| `web_frontend` | Vue 3 + Element Plus + ECharts 前端（Owner / Consumer / Admin 页面） |
| `web_gateway` | Spring Boot REST 网关：认证、会话、角色与资源归属校验、区块链存证服务 |
| `traffic_access_core` | Java 核心依赖模块：流加密、密钥派生、策略、查询、统计与通信协议（旧客户端及可复用业务逻辑） |
| `secure_stream_store` | 密文数据存储、时间片索引与 Kafka 消费服务（:1101） |
| `privacy_policy_controller` | 隐私策略、查询令牌、联邦密钥协商与 MPC 服务（:1102） |
| `traffic_stream_producer` | 交通流数据生产、加密与上传服务（:1234） |
| `test` | 演示用交通流量 CSV 测试数据（道路事故 / 拥堵对照 / 大型活动 / 机场通道等场景） |
| `data` | 数据库初始化脚本（`sys_plus.sql`）及运行时上传文件（不入库） |
| `config` | 数据库与区块链配置（敏感配置不入库，见 `database.properties.example`） |
| `docs/plans` | 功能设计文档（超级管理员 / 区块链存证 / 联邦授权链等） |

## 🛠️ 环境要求

| 依赖 | 版本/说明 |
| --- | --- |
| JDK | 17+ |
| Maven | 3.6+ |
| MySQL | 8.x，默认 `localhost:3306` |
| Kafka | 默认 `localhost:9092` |
| Node.js / npm | 18+ |
| Docker（可选） | 用于本地 Anvil 开发链（Foundry 镜像 `ghcr.io/foundry-rs/foundry:stable`） |

## 🚀 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS attempt DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p attempt < data/sys_plus.sql
```

### 2. 配置数据库连接

```bash
cp config/database.properties.example config/database.properties
# 按需修改 DB_URL / DB_USER / DB_PASSWORD
```

> ⚠️ `config/database.properties` 已被 `.gitignore` 忽略，真实密码不会进入版本库；部署时也可用环境变量覆盖。

### ⚙️ 运行前必改的本地配置（恢复真实凭据）

仓库**不包含任何真实密码或私钥**。为了安全，仓库中的示例与本地占位文件均为虚构值，正式运行前必须恢复：

| 文件 | 需要恢复的内容 | 恢复方法 |
| --- | --- | --- |
| `config/database.properties` | `DB_PASSWORD`（当前为占位值 `CHANGE-ME-FAKE-PASSWORD-2026`） | 改为你的真实 MySQL 密码（该文件不入库） |
| `config/blockchain-local.env` | `BLOCKCHAIN_PRIVATE_KEY`（当前为占位值 `f00d...`） | 删除本文件与 `anvil-local.secret` 后运行 `bash start-local-chain.sh`，自动生成全新的随机开发账户 |
| `config/anvil-local.secret` | 本地链助记词（当前为公开的测试助记词） | 同上，随 `start-local-chain.sh` 自动重新生成 |

同理，以下均为**本地运行时生成、不入库**的内容：

- `data/local-chain/`：Anvil 本地链状态；
- `data/uploads/`：通过页面上传的 CSV 与状态文件（演示数据见 `test/`）；
- 各模块 `src/main/java/key/*.jks`：用户/生产者密钥库，首次注册时自动生成。

### 3. 构建并启动后端

```bash
# 首次或代码变更后：构建四个模块并启动
bash start-backend.sh --build

# 仅重启已有 JAR（自动检测并重启运行中的托管服务）
bash start-backend.sh

# 单独启动本地 Anvil 开发链（可选，无 Foundry 镜像时后端跳过链启动）
bash start-local-chain.sh
```

脚本会依次启动四个 Java 后端，并在 `logs/` 下生成日志与 PID 文件：

| 服务 | 端口 |
| --- | --- |
| secure_stream_store（安全数据存储） | 1101 |
| privacy_policy_controller（隐私策略控制器） | 1102 |
| traffic_stream_producer（交通流生产） | 1234 |
| web_gateway（Web 网关） | 8080 |

健康检查：`curl http://localhost:8080/api/health`

### 4. 启动前端

```bash
npm --prefix web_frontend install   # 首次
npm --prefix web_frontend run dev
```

浏览器访问 **http://localhost:5173**（`/api` 已由 Vite 代理到 `:8080`，无跨域问题）。

## 🧑‍💻 使用指南

系统包含三类角色：

### 数据拥有者（Owner）
1. 登录后进入「设备管理」，注册路侧设备 / Producer 节点（IP 与端口）。
2. 进入「数据管理」创建交通数据流，设置名称、时间范围与最小粒度。
3. 上传 CSV 真实数据（或使用模拟数据，见 `test/` 目录）。
4. 在「策略管理」为指定 Consumer 创建普通策略或联邦策略（多流）。
5. 在「数据查询」查看自己名下数据流的统计结果。

### 数据消费者（Consumer）
1. 查看授权给自己的数据流，在授权时间与粒度范围内发起**普通查询**。
2. 勾选至少两个授权联邦策略，发起**联邦查询**，获得公共授权区间内的均值、方差、车流量与趋势图，而非原始 CSV。

### 超级管理员（特殊 Owner，默认账号 `9000` / `admin123`）
- 系统总览、用户状态管理、任意数据流/策略查看与删除、审计日志、异常流量分析与区块链存证状态查看与手动重试。
- 生产部署必须通过 `SUPER_ADMIN_NUMBER` / `SUPER_ADMIN_NAME` / `SUPER_ADMIN_PASSWORD` 环境变量覆盖默认演示凭据。

### ⏱️ 上传 CSV 数据的限制（重要）

上传前请先核对数据流的时间范围，**CSV 数据的时间必须落在数据流的起止时间内**（创建数据流时设置的 `startTime ~ endTime`），建议直接按 CSV 的实际时间范围创建数据流。

| 限制项 | 规则 |
| --- | --- |
| **时间有效性** ⚠️ | 每行时间戳必须在数据流 `[startTime, endTime]` 区间内，**越界行会被忽略**；若全部越界，系统会将数据按流起始时间与最小粒度重新映射时间轴（与原始 CSV 时间无关），流时间过短装不下任何点时上传会失败并提示「没有可写入的数据」 |
| 文件格式 | 仅支持 `.csv`（UTF-8），首行可为表头；空文件视为使用模拟数据 |
| 文件大小 | 单文件最大 **100MB** |
| 数据量 | 单次最多 **3000 个点**，超出会自动等距抽样（页面上标记为 `sampled`） |
| 时间列 | 表头识别 `timestamp / time / date / 时间 / 日期`；也支持无时间列——此时按流起始时间 + 行号 × 最小粒度自动生成时间 |
| 数值列 | 表头优先识别 `vehicle_flow / traffic_flow / flow / count / 车流量 / 流量` 等；或使用 `value / 数值`；也支持单列纯数值文件 |
| 时间格式 | 秒或毫秒时间戳；`yyyy-MM-dd HH:mm:ss`；`yyyy-MM-dd HH:mm`；ISO 本地时间（`T` 分隔）；带时区的 ISO 时间 |
| 数值格式 | 支持整数或小数（自动四舍五入为整数） |

> 💡 联邦查询的读取步长与数据流最小粒度相关，多流联合分析时建议各数据流**统一时间范围与采样间隔**，否则公共授权区间可能过短。

### CSV 测试数据

`test/` 目录提供了现成的演示数据（`timestamp,vehicle_flow` 格式，30 秒采样，时间为 2026-08-20 全天，创建数据流时请将起止时间设置为覆盖该范围）：

```csv
timestamp,vehicle_flow
2026-08-20 00:00:00,749
2026-08-20 00:00:30,744
```

| 文件 | 场景 |
| --- | --- |
| `全天_道路A_事故异常_2026-08-20.csv` | 早高峰事故导致的流量异常 |
| `道路B_联邦查询_拥堵对照.csv` | 联邦查询的拥堵对照数据 |
| `道路基线_平稳流量.csv` / `全天_道路基线_平稳流量_2026-08-20.csv` | 平稳流量基线 |
| `全天_道路C_大型活动疏散_2026-08-20.csv` | 大型活动人流疏散 |
| `全天_道路D_机场通道_2026-08-20.csv` | 机场通道流量 |
| `全天_高速入口_潮汐流量_2026-08-20.csv` | 高速入口潮汐流量 |

> 建议配合上表理解：`test/` 内 CSV 时间均为 2026-08-20 全天，创建数据流时把起止时间设为覆盖该日期即可直接上传使用。

## 🔐 安全说明

- 所有业务请求以 `X-Token` 标识会话；Owner 只能操作名下资源，Consumer 只能查询授权数据流，查询时间/粒度受策略严格约束。
- 区块链私钥仅通过环境变量 `BLOCKCHAIN_PRIVATE_KEY` 注入，本地开发链账户（`config/blockchain-local.env`、`anvil-local.secret`）与密钥库（`*.jks`）均不入库。
- 链上仅存 SHA-256 摘要与业务元数据，不存原始数据、完整密文或任何密钥。
- 本地 Anvil 链（`127.0.0.1:8545`，chain ID `31337`）仅用于开发与演示，接入公网（如 Sepolia）时必须更换全新账户。

