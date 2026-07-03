# ChipYard
ChipMore Dorm Management



## c9dc615 chore: harden docker database deployment 20260703
main -> origin/main
## 部署安全化改造

### 背景
项目已部署到服务器，生产 MySQL 中会产生真实数据。原先生产环境仍保留 `SQL_INIT_MODE=always` 的初始化风险，后端启动时可能重复执行 `schema.sql/data.sql`，不适合后续持续部署。

### 改动
- 引入 Flyway 管理生产数据库迁移。
- 默认本地环境继续使用 H2 + `schema.sql/data.sql`。
- 生产 `prod` 环境关闭 Spring SQL 初始化：`spring.sql.init.mode=never`。
- 生产 `prod` 环境启用 Flyway：`classpath:db/migration`。
- 开启 `baseline-on-migrate`，兼容服务器已有 MySQL 数据库。
- 从 `docker-compose.yml` 和 `.env.example` 移除 `SQL_INIT_MODE`。
- 新增生产迁移脚本：
  - `V1__init_schema.sql`
  - `V2__demo_seed_data.sql`
- 新增部署文档：`docs/DEPLOY.md`。

### 验证
- 后端测试通过：`mvn test`
- 共 `120` 个测试通过。
