CREATE TABLE IF NOT EXISTS `shop` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'shop id',
  `name` varchar(128) NOT NULL COMMENT 'shop name',
  `is_self` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'self operated flag',
  `shipping_type` varchar(32) NOT NULL COMMENT 'shipping rule type',
  `shipping_fee` int NOT NULL DEFAULT '0' COMMENT 'shipping fee in cents',
  `free_shipping_threshold` int NOT NULL DEFAULT '0' COMMENT 'free shipping threshold in cents',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='shop';

SET @shop_id_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'item'
    AND COLUMN_NAME = 'shop_id'
);
SET @shop_id_sql = IF(
  @shop_id_exists = 0,
  'ALTER TABLE `item` ADD COLUMN `shop_id` bigint DEFAULT NULL COMMENT ''shop id'' AFTER `category_id`',
  'SELECT 1'
);
PREPARE stmt_add_shop_id FROM @shop_id_sql;
EXECUTE stmt_add_shop_id;
DEALLOCATE PREPARE stmt_add_shop_id;

INSERT INTO `shop` (`id`, `name`, `is_self`, `shipping_type`, `shipping_fee`, `free_shipping_threshold`, `status`)
VALUES
  (1001, '优衣库官方旗舰店', 1, 'THRESHOLD_FREE', 1000, 9900, 1),
  (1002, '小米官方旗舰店', 1, 'FREE', 0, 0, 1),
  (1003, '宜家家居旗舰店', 0, 'FIXED', 3000, 0, 1),
  (1004, 'MAC官方旗舰店', 0, 'FREE', 0, 0, 1),
  (1005, '三只松鼠旗舰店', 0, 'THRESHOLD_FREE', 800, 9900, 1),
  (1006, '探路者官方旗舰店', 1, 'THRESHOLD_FREE', 1200, 19900, 1),
  (1007, '好奇母婴旗舰店', 0, 'THRESHOLD_FREE', 800, 9900, 1),
  (1008, '倍思车品旗舰店', 0, 'FIXED', 1000, 0, 1),
  (1009, '机械工业出版社图书专营店', 0, 'THRESHOLD_FREE', 600, 5900, 1),
  (1010, '潘多拉官方旗舰店', 0, 'FREE', 0, 0, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `is_self` = VALUES(`is_self`),
  `shipping_type` = VALUES(`shipping_type`),
  `shipping_fee` = VALUES(`shipping_fee`),
  `free_shipping_threshold` = VALUES(`free_shipping_threshold`),
  `status` = VALUES(`status`);

UPDATE `item`
SET `shop_id` = CASE `id`
  WHEN 1 THEN 1001
  WHEN 2 THEN 1002
  WHEN 3 THEN 1003
  WHEN 4 THEN 1004
  WHEN 5 THEN 1005
  WHEN 6 THEN 1006
  WHEN 7 THEN 1007
  WHEN 8 THEN 1008
  WHEN 9 THEN 1009
  WHEN 10 THEN 1010
  ELSE `shop_id`
END
WHERE `id` IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  AND (`shop_id` IS NULL OR `shop_id` = 0);
