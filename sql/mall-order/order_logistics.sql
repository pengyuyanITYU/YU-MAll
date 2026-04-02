
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `order_logistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_logistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物流记录ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `logistics_no` varchar(64) NOT NULL DEFAULT '' COMMENT '物流单号(快递单号)',
  `logistics_company` varchar(32) NOT NULL DEFAULT '' COMMENT '物流公司名称(如:顺丰,京东)',
  `logistics_code` varchar(32) DEFAULT '' COMMENT '物流公司编码(用于调API,如:SF, JD)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '物流状态: 1-已发货, 2-运输中, 3-已签收, 4-异常',
  `is_signed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已签收: 0-否, 1-是',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `sign_time` datetime DEFAULT NULL COMMENT '签收时间',
  `logistics_info` json DEFAULT NULL COMMENT '物流轨迹详情(存储第三方API返回的JSON数据)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE COMMENT '订单查询索引',
  KEY `idx_logistics_no` (`logistics_no`) USING BTREE COMMENT '物流单号查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单物流/发货记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

