UPDATE item_comment
SET status = 1
WHERE deleted = 0
  AND status = 0;
