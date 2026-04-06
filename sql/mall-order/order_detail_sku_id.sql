SET @column_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'order_detail'
    AND COLUMN_NAME = 'sku_id'
);

SET @ddl := IF(
  @column_exists = 0,
  'ALTER TABLE `order_detail` ADD COLUMN `sku_id` BIGINT NULL COMMENT ''商品SKU ID'' AFTER `item_id`',
  'SELECT ''order_detail.sku_id already exists'' AS message'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
