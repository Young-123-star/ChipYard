-- 旧版生产容器使用 UTC 生成 LocalDateTime，业务时间统一按 Asia/Shanghai 保存。
-- Flyway 仅执行一次，用于修正升级前已缴账单的缴费时间。
UPDATE dms_fee_bill
SET paid_at = DATE_ADD(paid_at, INTERVAL 8 HOUR)
WHERE paid_at IS NOT NULL;