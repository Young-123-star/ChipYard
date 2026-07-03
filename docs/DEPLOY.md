# ChipYard Docker 部署说明

## 日常部署

```bash
cd ~/ChipYard
git pull --ff-only origin main
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 backend
```

## 数据库初始化策略

生产环境使用 Flyway 管理 MySQL 表结构，不再使用 Spring Boot 的 `schema.sql` 自动初始化。

- 本地默认环境：继续使用 H2 + `db/schema.sql` + `db/data.sql`。
- 生产 `prod` 环境：使用 `db/migration/V*.sql`。
- 已有服务器数据库：`baseline-on-migrate: true` 会把当前库标记为 V1，避免重复建表。
- 新服务器空数据库：Flyway 会自动执行 `V1__init_schema.sql` 和 `V2__demo_seed_data.sql`。

## 首次启用 Flyway 前备份

```bash
cd ~/ChipYard
docker compose exec mysql mysqldump -u root -p dms > backup-before-flyway.sql
```

## 确认 Flyway 状态

```bash
docker compose exec mysql mysql -u root -p dms -e "select installed_rank, version, description, success from flyway_schema_history order by installed_rank;"
```

已有数据库第一次升级后，通常会看到 `<< Flyway Baseline >>` 和后续迁移记录。

## 新增数据库变更

不要直接改生产库，也不要改旧的 `V*.sql` 文件。新增一个版本文件，例如：

```text
dms-backend/src/main/resources/db/migration/V3__add_repair_rating.sql
```

内容示例：

```sql
ALTER TABLE dms_repair_order ADD COLUMN rating TINYINT NULL;
```

部署后后端启动时会自动执行一次。

## 停止和重启

```bash
docker compose stop
docker compose start
docker compose restart backend
```

不要执行：

```bash
docker compose down -v
```

`-v` 会删除 MySQL 数据卷。