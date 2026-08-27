/*
 Navicat Premium Data Transfer

 Source Server         : sql
 Source Server Type    : MySQL
 Source Server Version : 80037
 Source Host           : localhost:3306
 Source Schema         : attempt

 Target Server Type    : MySQL
 Target Server Version : 80037
 File Encoding         : 65001

 Date: 26/07/2024 16:52:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for custom
-- ----------------------------
DROP TABLE IF EXISTS `custom`;
CREATE TABLE `custom`  (
  `number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `usr_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `identity` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of custom
-- ----------------------------
INSERT INTO `custom` VALUES ('1', 'qiao', '1', '拥有者');
INSERT INTO `custom` VALUES ('1001', 'MING', '1', '拥有者');
INSERT INTO `custom` VALUES ('1002', 'HONG', '1', '拥有者');
INSERT INTO `custom` VALUES ('1003', 'GANG', '1', '拥有者');
INSERT INTO `custom` VALUES ('1004', 'LAN', '1', '拥有者');
INSERT INTO `custom` VALUES ('1005', 'LI', '1', '拥有者');
INSERT INTO `custom` VALUES ('11', '11', '11', '消费者');
INSERT INTO `custom` VALUES ('1101', 'Consumer1', '1', '消费者');
INSERT INTO `custom` VALUES ('1111', 'TESTTT', '1111', '拥有者');
INSERT INTO `custom` VALUES ('2', 'ao', '2', '拥有者');
INSERT INTO `custom` VALUES ('3', 'kang', '3', '拥有者');
INSERT INTO `custom` VALUES ('4', 'test', '4', '拥有者');
INSERT INTO `custom` VALUES ('5', 'HONG', '5', '消费者');
INSERT INTO `custom` VALUES ('6666', '6666', '6666', '拥有者');
INSERT INTO `custom` VALUES ('7777', 'tt', '7777', '拥有者');
INSERT INTO `custom` VALUES ('8800', '5555', '5555', '拥有者');
-- 超级管理员由 Web Gateway 启动时根据 SUPER_ADMIN_* 环境变量创建并保证唯一，
-- 不在初始化脚本中保存默认密码。

-- ----------------------------
-- Super administrator audit and account state
-- ----------------------------
DROP TABLE IF EXISTS `admin_audit_log`;
CREATE TABLE `admin_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `trace_id` varchar(64) NOT NULL,
  `event_time` bigint NOT NULL,
  `actor_number` varchar(255) NULL,
  `actor_name` varchar(255) NULL,
  `actor_role` varchar(32) NULL,
  `action` varchar(64) NOT NULL,
  `method` varchar(16) NULL,
  `request_path` varchar(512) NULL,
  `target_type` varchar(64) NULL,
  `target_id` varchar(255) NULL,
  `detail` text NULL,
  `success` tinyint(1) NOT NULL,
  `duration_ms` bigint NOT NULL DEFAULT 0,
  `source_ip` varchar(128) NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_audit_time` (`event_time`),
  INDEX `idx_audit_trace` (`trace_id`),
  INDEX `idx_audit_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `user_account_status`;
CREATE TABLE `user_account_status` (
  `user_number` varchar(255) NOT NULL,
  `disabled` tinyint(1) NOT NULL DEFAULT 0,
  `updated_at` bigint NOT NULL,
  `updated_by` varchar(255) NULL,
  `reason` varchar(500) NULL,
  PRIMARY KEY (`user_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for eq
-- ----------------------------
DROP TABLE IF EXISTS `eq`;
CREATE TABLE `eq`  (
  `main_key` int NOT NULL AUTO_INCREMENT,
  `eq_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `owner_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`main_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of eq
-- ----------------------------
INSERT INTO `eq` VALUES (2, '2', '1002', '心率传感器', '9023', '192.168.128.4');
INSERT INTO `eq` VALUES (7, '7', '4', '手环', '9004', '192.168.129.4');
INSERT INTO `eq` VALUES (8, '8', '1003', '手表', '9033', '192.168.123.3');
INSERT INTO `eq` VALUES (9, '9', '1004', '手表', '9044', '192.168.124.4');
INSERT INTO `eq` VALUES (11, '1', '6666', '111', '11', '11');
INSERT INTO `eq` VALUES (12, '1', '7777', '77', '77', '77');
INSERT INTO `eq` VALUES (13, '1', '1', 'test', '1234', '127.0.0.1');
INSERT INTO `eq` VALUES (14, '2', '1', 'test2', '1234', '127.0.0.1');
INSERT INTO `eq` VALUES (15, '1', '2', 'test02', '1234', '127.0.0.1');
INSERT INTO `eq` VALUES (16, '1', '3', 'test03', '1234', '127.0.0.1');
INSERT INTO `eq` VALUES (17, '3', '1', 'test03', '1234', '127.0.0.1');

-- ----------------------------
-- Table structure for history
-- ----------------------------
DROP TABLE IF EXISTS `history`;
CREATE TABLE `history`  (
  `usrname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `time` bigint NULL DEFAULT NULL,
  `streamid` bigint NULL DEFAULT NULL,
  `starttime` bigint NULL DEFAULT NULL,
  `endtime` bigint NULL DEFAULT NULL,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `streamid_mpc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of history
-- ----------------------------
INSERT INTO `history` VALUES ('11', '单流查询', 1721809306000, 3296525552710579299, 1721699160000, 1721706299999, 'src/main/java/data/1.jpg', NULL);
INSERT INTO `history` VALUES ('11', '单流查询', 1721809313000, 3296525552710579299, 1721699160000, 1721706299999, 'src/main/java/data/1.jpg', NULL);
INSERT INTO `history` VALUES ('11', '联邦查询', 1721809600000, NULL, 1719883246000, 1719893854000, 'src/main/java/data/1.jpg', '-5791682683127775308+-9164399521115693798');
INSERT INTO `history` VALUES ('11', '联邦查询', 1721891229000, NULL, 1721699475000, 1721706299000, 'src/main/java/data/1.jpg', '3296525552710579299+-7432411084493487157+2878341750899706185');
INSERT INTO `history` VALUES ('11', '单流查询', 1721893385000, -5697705943604644599, 1719909935000, 1719924334999, 'src/main/java/data/1.jpg', NULL);
INSERT INTO `history` VALUES ('11', '单流查询', 1721893390000, -5697705943604644599, 1719909935000, 1719924334999, 'src/main/java/data/1.jpg', NULL);
INSERT INTO `history` VALUES ('11', '单流查询', 1721979608000, 3296525552710579299, 1721699160000, 1721706299999, 'src/main/java/data/1.jpg', NULL);
INSERT INTO `history` VALUES ('11', '单流查询', 1721979612000, 3296525552710579299, 1721699160000, 1721706299999, 'src/main/java/data/1.jpg', NULL);
INSERT INTO `history` VALUES ('11', '单流查询', 1721979650000, 8739713910934350248, 1721979370000, 1721986569999, 'src/main/java/data/1.jpg', NULL);
INSERT INTO `history` VALUES ('11', '联邦查询', 1721979665000, NULL, 1721699475000, 1721706299000, 'src/main/java/data/1.jpg', '3296525552710579299+-7432411084493487157+2878341750899706185');

-- ----------------------------
-- Table structure for owner_stream
-- ----------------------------
DROP TABLE IF EXISTS `owner_stream`;
CREATE TABLE `owner_stream`  (
  `owner_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `stream_id` bigint NOT NULL,
  INDEX `owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `stream_id`(`stream_id` ASC) USING BTREE,
  CONSTRAINT `owner_id` FOREIGN KEY (`owner_id`) REFERENCES `custom` (`number`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `stream_id` FOREIGN KEY (`stream_id`) REFERENCES `stream` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of owner_stream
-- ----------------------------
INSERT INTO `owner_stream` VALUES ('1', -5791682683127775308);
INSERT INTO `owner_stream` VALUES ('1', -9164399521115693798);
INSERT INTO `owner_stream` VALUES ('2', -6160517888319165075);
INSERT INTO `owner_stream` VALUES ('1', 6292524579467323367);
INSERT INTO `owner_stream` VALUES ('1', 2580904396591251987);
INSERT INTO `owner_stream` VALUES ('2', 1082359770862159408);
INSERT INTO `owner_stream` VALUES ('3', -5697705943604644599);
INSERT INTO `owner_stream` VALUES ('1', 3296525552710579299);
INSERT INTO `owner_stream` VALUES ('2', -7432411084493487157);
INSERT INTO `owner_stream` VALUES ('3', 2878341750899706185);
INSERT INTO `owner_stream` VALUES ('1', 8739713910934350248);

-- ----------------------------
-- Table structure for stream_producer
-- ----------------------------
DROP TABLE IF EXISTS `stream_producer`;
CREATE TABLE `stream_producer`  (
  `stream_id` bigint NOT NULL,
  `owner_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `producer_id` bigint NOT NULL,
  `producer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`stream_id`) USING BTREE,
  INDEX `owner_id_idx`(`owner_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for policy
-- ----------------------------
DROP TABLE IF EXISTS `policy`;
CREATE TABLE `policy`  (
  `owner_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `consumer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `policy_id` bigint NOT NULL,
  `stream_id` bigint NOT NULL,
  `p_starttime` bigint NOT NULL,
  `p_endtime` bigint NOT NULL,
  `multiple` bigint NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of policy
-- ----------------------------
INSERT INTO `policy` VALUES ('qiao', '11', 7429021697254532381, -5791682683127775308, 1719883055000, 1719893854999, 1);
INSERT INTO `policy` VALUES ('qiao', '11', -8142744123036016596, -9164399521115693798, 1719883246000, 1719897645999, 1);
INSERT INTO `policy` VALUES ('ao', '11', 5656547391273277907, -6160517888319165075, 1719883331000, 1719897730999, 1);
INSERT INTO `policy` VALUES ('qiao', '11', -8957220111982462635, 6292524579467323367, 1719909744000, 1719920543999, 1);
INSERT INTO `policy` VALUES ('qiao', '11', -8670191855056346723, 2580904396591251987, 1719909841000, 1719924240999, 1);
INSERT INTO `policy` VALUES ('ao', '11', 2244439868758247510, 1082359770862159408, 1719909884000, 1719924283999, 1);
INSERT INTO `policy` VALUES ('kang', '11', -8364363628476437289, -5697705943604644599, 1719909935000, 1719924334999, 1);
INSERT INTO `policy` VALUES ('qiao', 'HONG', 606952745947431010, -5791682683127775308, 1719883055000, 1719886654999, 1);
INSERT INTO `policy` VALUES ('qiao', 'Consumer1', 4718447492675836981, -5791682683127775308, 1719883055000, 1719886654999, 1);
INSERT INTO `policy` VALUES ('qiao', '11', -1861007801580197887, 3296525552710579299, 1721699160000, 1721706299999, 1);
INSERT INTO `policy` VALUES ('ao', '11', 786211635745721834, -7432411084493487157, 1721699369000, 1721710168999, 1);
INSERT INTO `policy` VALUES ('kang', '11', 3925276163648523022, 2878341750899706185, 1721699475000, 1721710274999, 1);
INSERT INTO `policy` VALUES ('qiao', '11', 5616213814963428293, 8739713910934350248, 1721979370000, 1721986569999, 1);

-- ----------------------------
-- Table structure for policy_mpc
-- ----------------------------
DROP TABLE IF EXISTS `policy_mpc`;
CREATE TABLE `policy_mpc`  (
  `owner_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `consumer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `policy_id` bigint NOT NULL,
  `stream_id` bigint NOT NULL,
  `p_starttime` bigint NOT NULL,
  `p_endtime` bigint NOT NULL,
  `mingranularity` bigint NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of policy_mpc
-- ----------------------------
INSERT INTO `policy_mpc` VALUES ('qiao', '11', 7415116798377450573, -5791682683127775308, 1719883055000, 1719893854999, 1000);
INSERT INTO `policy_mpc` VALUES ('qiao', '11', -265853326974909517, -9164399521115693798, 1719883246000, 1719897645999, 1000);
INSERT INTO `policy_mpc` VALUES ('ao', '11', 739853134844461979, -6160517888319165075, 1719883331000, 1719897730999, 1000);
INSERT INTO `policy_mpc` VALUES ('qiao', '11', 8235128116216350216, 6292524579467323367, 1719909744000, 1719920543999, 1000);
INSERT INTO `policy_mpc` VALUES ('qiao', '11', -9129346988359257226, 2580904396591251987, 1719909841000, 1719924240999, 1000);
INSERT INTO `policy_mpc` VALUES ('ao', '11', 4679585129000780727, 1082359770862159408, 1719909884000, 1719924283999, 1000);
INSERT INTO `policy_mpc` VALUES ('kang', '11', -1109904728132389699, -5697705943604644599, 1719909935000, 1719924334999, 1000);
INSERT INTO `policy_mpc` VALUES ('qiao', '11', 4820881331423548363, 3296525552710579299, 1721699160000, 1721706299999, 1000);
INSERT INTO `policy_mpc` VALUES ('ao', '11', 1580269495824573802, -7432411084493487157, 1721699369000, 1721710168999, 1000);
INSERT INTO `policy_mpc` VALUES ('kang', '11', 6322967926526795871, 2878341750899706185, 1721699475000, 1721710274999, 1000);
INSERT INTO `policy_mpc` VALUES ('qiao', '11', -2293571199604434532, 8739713910934350248, 1721979370000, 1721986569999, 1000);

-- ----------------------------
-- Table structure for stream
-- ----------------------------
DROP TABLE IF EXISTS `stream`;
CREATE TABLE `stream`  (
  `id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `starttime` bigint NOT NULL,
  `endtime` bigint NOT NULL,
  `mingranularity` bigint NOT NULL,
  `granularity` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of stream
-- ----------------------------
INSERT INTO `stream` VALUES (-9164399521115693798, '702tt2', '心率', 1719883246000, 1719897645999, 1000, 10000);
INSERT INTO `stream` VALUES (-7432411084493487157, 'test002', '心率', 1721699369000, 1721710168999, 1000, 10000);
INSERT INTO `stream` VALUES (-6160517888319165075, '702tt3ao', '心率', 1719883331000, 1719897730999, 1000, 10000);
INSERT INTO `stream` VALUES (-5791682683127775308, '702tt', '心率', 1719883055000, 1719893854999, 1000, 10000);
INSERT INTO `stream` VALUES (-5697705943604644599, '702tt007', '心率', 1719909935000, 1719924334999, 1000, 10000);
INSERT INTO `stream` VALUES (1082359770862159408, '702tt006', '心率', 1719909884000, 1719924283999, 1000, 10000);
INSERT INTO `stream` VALUES (2580904396591251987, '702tt005', '心率', 1719909841000, 1719924240999, 1000, 10000);
INSERT INTO `stream` VALUES (2878341750899706185, 'test003', '心率', 1721699475000, 1721710274999, 1000, 10000);
INSERT INTO `stream` VALUES (3296525552710579299, 'test001', '心率', 1721699160000, 1721706359999, 1000, 10000);
INSERT INTO `stream` VALUES (6292524579467323367, '702tt004', '心率', 1719909744000, 1719920543999, 1000, 10000);
INSERT INTO `stream` VALUES (8739713910934350248, '0726001', '心率', 1721979370000, 1721986569999, 1000, 10000);

-- Blockchain provenance outbox and computation trace
DROP TABLE IF EXISTS `computation_trace`;
DROP TABLE IF EXISTS `blockchain_anchor`;
CREATE TABLE `blockchain_anchor` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `trace_id` varchar(64) NOT NULL,
  `business_type` varchar(48) NOT NULL,
  `business_id` varchar(255) NOT NULL,
  `payload_json` longtext NULL,
  `payload_sha256` char(64) NULL,
  `status` varchar(24) NOT NULL DEFAULT 'PENDING',
  `retry_count` int NOT NULL DEFAULT 0,
  `max_retries` int NOT NULL DEFAULT 5,
  `next_attempt_at` bigint NOT NULL DEFAULT 0,
  `locked_at` bigint NULL,
  `last_error` varchar(2000) NULL,
  `chain_id` bigint NULL,
  `from_address` varchar(128) NULL,
  `transaction_hash` varchar(128) NULL,
  `block_number` bigint NULL,
  `confirmed_at` bigint NULL,
  `created_at` bigint NOT NULL,
  `updated_at` bigint NOT NULL,
  UNIQUE KEY `uk_anchor_business` (`business_type`,`business_id`),
  KEY `idx_anchor_work` (`status`,`next_attempt_at`),
  KEY `idx_anchor_trace` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `computation_trace` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `trace_id` varchar(64) NOT NULL,
  `stream_id` bigint NOT NULL,
  `stage` varchar(64) NOT NULL,
  `start_time` bigint NULL,
  `end_time` bigint NULL,
  `algorithm` varchar(128) NULL,
  `input_sha256` char(64) NULL,
  `output_sha256` char(64) NULL,
  `anomaly` tinyint(1) NOT NULL DEFAULT 0,
  `metadata_json` longtext NULL,
  `created_at` bigint NOT NULL,
  UNIQUE KEY `uk_computation_stage` (`trace_id`,`stage`),
  KEY `idx_computation_stream` (`stream_id`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TRIGGER IF EXISTS `trg_policy_anchor`;
CREATE TRIGGER `trg_policy_anchor` AFTER INSERT ON `policy` FOR EACH ROW
INSERT IGNORE INTO `blockchain_anchor` (`trace_id`,`business_type`,`business_id`,`status`,`created_at`,`updated_at`)
VALUES (REPLACE(UUID(),'-',''),'POLICY',CAST(NEW.policy_id AS CHAR),'PENDING',CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS UNSIGNED),CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS UNSIGNED));
DROP TRIGGER IF EXISTS `trg_policy_mpc_anchor`;
CREATE TRIGGER `trg_policy_mpc_anchor` AFTER INSERT ON `policy_mpc` FOR EACH ROW
INSERT IGNORE INTO `blockchain_anchor` (`trace_id`,`business_type`,`business_id`,`status`,`created_at`,`updated_at`)
VALUES (REPLACE(UUID(),'-',''),'FEDERATION_POLICY',CAST(NEW.policy_id AS CHAR),'PENDING',CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS UNSIGNED),CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS UNSIGNED));

SET FOREIGN_KEY_CHECKS = 1;
