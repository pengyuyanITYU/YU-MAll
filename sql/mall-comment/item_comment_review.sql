SET @schema_name = DATABASE();
SET @release_cutoff = '2026-04-05 18:00:00';

SET @has_reject_reason = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'item_comment'
    AND COLUMN_NAME = 'reject_reason'
);

SET @ddl = IF(
  @has_reject_reason = 0,
  'ALTER TABLE item_comment ADD COLUMN reject_reason varchar(255) DEFAULT NULL COMMENT ''驳回原因'' AFTER status',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE item_comment
SET status = 1,
    reject_reason = NULL
WHERE deleted = 0
  AND create_time < @release_cutoff
  AND (status IS NULL OR status = 0);
