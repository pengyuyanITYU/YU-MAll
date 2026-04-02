
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
DROP TABLE IF EXISTS `item_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint DEFAULT NULL COMMENT '具体规格ID(SKU)，如果商品无规格可为空',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_detail_id` bigint NOT NULL COMMENT '订单明细ID(防止一个订单买多个相同商品产生混淆)',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称(冗余字段，减少连表查询)',
  `user_avatar` varchar(255) DEFAULT NULL COMMENT '用户头像(冗余字段)',
  `rating` tinyint(1) NOT NULL DEFAULT '5' COMMENT '评分星级(1-5)',
  `content` text COMMENT '评价内容',
  `images` json DEFAULT NULL COMMENT '评价图片/视频列表，JSON数组格式 ["url1", "url2"]',
  `is_anonymous` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否匿名(0:否 1:是)',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '审核状态(0:待审核 1:审核通过 2:审核拒绝/隐藏)',
  `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶(0:否 1:是)',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `reply_count` int NOT NULL DEFAULT '0' COMMENT '回复数(追评数)',
  `merchant_reply_content` text COMMENT '商家回复内容',
  `merchant_reply_time` datetime DEFAULT NULL COMMENT '商家回复时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0:正常 1:删除)',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

