# AGENTS.md

本文件面向 AI 编码代理，介绍本仓库（ChipYard，公司宿舍管理系统 DMS）的架构、构建、测试与部署约定。

## 项目概览

一套前后端分离的「公司宿舍管理系统」，管理楼栋/楼层/房间/床位资源、居住人、入住/退宿、住宿费账单、水电抄表结算、维修工单、巡检、字典与数据导入导出，并提供与 OA / HCP 系统的集成接口（webhook）。

- `dms-backend/` — Spring Boot 3.2.5 + Java 17 后端（Maven）。
- `dms-frontend/` — Vue 3 + TypeScript + Vite 前端。
- `docker-compose.yml` — 生产编排：mysql / backend / frontend（nginx）。
- `docs/` — 需求说明、PR 记录、部署手册（`DEPLOY.md`）、测试文档（`docs/testing/`）等，均为中文。

## 技术栈

### 后端（`dms-backend/`）

- Spring Boot 3.2.5，Java 17，Maven（`pom.xml`）。
- Spring Security + JWT（jjwt 0.12.5）无状态认证；BCrypt 密码哈希。
- MyBatis-Plus 3.5.5（`mybatis-plus-spring-boot3-starter`），逻辑删除字段 `deletedAt`。
- 数据库：本地/测试用 H2（MySQL 兼容模式），生产用 MySQL 8 + Flyway 迁移。
- Knife4j 4.5.0（OpenAPI3 文档，`/doc.html`）、Apache POI（Excel 导入导出）、Lombok。

### 前端（`dms-frontend/`）

- Vue 3.4 + TypeScript + Vite 5 + vue-router 4 + Pinia 2。
- Element Plus 2.7（UI）+ ECharts / vue-echarts（报表图表）+ axios。
- 无前端单元测试框架；`npm run build` 内含 `vue-tsc -b` 类型检查。

## 构建与运行命令

### 后端

```bash
cd dms-backend
mvn spring-boot:run          # 本地运行（默认 profile：H2 内存库 + schema.sql/data.sql）
mvn test                     # 运行全部测试（CI 即执行此命令）
mvn -DskipTests package      # 打包 jar（Dockerfile 中使用）
```

本地默认配置（`application.yml`）：端口 8080，H2 内存库 `jdbc:h2:mem:dms;MODE=MySQL`，启动时执行 `db/schema.sql` + `db/data.sql`（含演示数据），Flyway 关闭，H2 控制台 `/h2-console`。

### 前端

```bash
cd dms-frontend
npm ci
npm run dev        # Vite 开发服务器，端口 5173，/api 代理到 http://localhost:8080
npm run build      # vue-tsc 类型检查 + 产物输出到 dist/
npm run preview
```

路径别名 `@` 指向 `src/`。axios 统一封装在 `src/utils/request.ts`（baseURL `/api`，自动带 `Authorization: Bearer <token>`，`code === 0` 视为成功并解包 `data`）。

### Docker（生产）

```bash
cp .env.example .env   # 配置 MYSQL_*、JWT_SECRET、INTEGRATION_TOKEN
docker compose up -d --build
```

前端 nginx 容器监听 80 端口并将 `/api/` 反代到 backend:8080。

## 代码组织

### 后端分层（`com.company.dms`）

- `common/` — 通用基础设施：
  - `result/`：`R<T>` 统一响应体（`code/message/data`，成功码 0）、`PageResult`、`ResultCode`。
  - `exception/`：`BizException` 业务异常 + `GlobalExceptionHandler` 全局异常处理。
  - `security/`：`SecurityConfig`（白名单含 `/api/auth/login`、`/api/integration/**`、swagger、h2-console）、`JwtAuthFilter`、`JwtUtil`、`SecurityUtil`。
  - `mybatis/`：`BaseEntity`、`MybatisPlusConfig`、自动填充 `MyMetaObjectHandler`。
- `module/` — 按业务域分包，每个模块内部为 `controller / service(+Impl) / mapper / entity / dto / vo` 六层结构：
  `auth`（登录/用户）、`resource`（楼栋/楼层/房间/床位）、`resident`（居住人）、`checkin`（入住意向单/入住档案）、`checkout`（退宿单/欠费挂账）、`fee`（收费标准/账单/抄表/水电结算/报表）、`repair`（维修工单）、`inspection`（巡检）、`dict`（字典）、`importer`（Excel 数据初始化导入）、`exporter`（导出）、`integration`（OA / HCP webhook，token 鉴权）。

### 前端分层（`src/`）

- `api/` — 按后端模块一一对应的 axios 接口封装（`fee.ts`、`checkin.ts` 等），`types.ts` 放共享类型。
- `views/` — 页面，按业务域分目录：`dashboard / resource / resident / checkin / checkout / fee / report / repair / inspection / system / import / login`。
- `router/index.ts` — 路由与登录守卫（未登录跳 `/login`）。**侧栏菜单由路由 meta 驱动**：业务路由的 `meta.group`（分组名）、`meta.icon`（@element-plus/icons-vue 组件）、`meta.menuTitle`（菜单显示名，缺省用 `meta.title`）决定菜单，新增页面只需加路由，不要改 layout。
- `stores/user.ts` — Pinia 用户态（token、当前用户）。
- `layout/index.vue` — 主布局（深色侧栏 + 顶栏，菜单从路由表生成）；`components/layout/` 布局组件（`AppPage` 页头、`DataView` 列表页骨架）；`utils/dict.ts` 字典工具；`composables/` 组合式函数。

## 编码约定

- 文档、注释、提交信息主要使用中文；保持现有风格。
- 数据库表名：业务表 `dms_` 前缀（如 `dms_room`、`dms_fee_bill`），系统表 `sys_` 前缀（`sys_user`、`sys_dict_type`）。
- 实体使用 MyBatis-Plus 注解 + Lombok，继承 `common/mybatis/BaseEntity`；逻辑删除字段 `deletedAt`（未删为 `null`，已删为 `now()`）。
- 所有 Controller 返回 `R<T>`；业务错误抛 `BizException`，由 `GlobalExceptionHandler` 统一转换，不要在 Controller 里手写 try/catch 返回。
- 接口路径统一 `/api/...`；`/api/integration/**` 为系统间集成接口，使用 `INTEGRATION_TOKEN` 而非 JWT。
- 最小改动原则：不要顺手重构无关代码；新增代码与所在文件既有写法保持一致。

### 前端视觉约定（蓝色主题 3.0）

- 设计令牌集中在 `src/styles/theme.css` 的 `:root`（`--dms-*` 系列：色板/侧栏/间距/字号/阴影/动效）；页面与组件样式**只允许引用令牌变量，禁止新增硬编码色值**（ECharts 等 JS 场景无法读 CSS 变量时，保持与令牌同值并加注释说明，见 `views/report/index.vue`）。
- 视觉基调：深色中性侧栏（`--dms-nav-*`）+ 中性灰内容区；卡片白底 + 1px `--dms-hairline` 发丝边框、**零阴影**（阴影仅浮层：弹窗/下拉/消息）；品牌蓝 `#2b5ce6` 只用于主按钮、选中态、关键数字。
- 列表页统一使用 `components/layout/DataView.vue` 骨架（`filters` / `filter-actions` / `actions` / 默认 / `pagination` 插槽；筛选 >3 个加 `collapsible`）；行内操作最多 2 个主按钮，其余收 `el-dropdown`「更多」；弹窗宽度两档：简单表单 480px、复杂表单 720px。
- 设计预览稿存于 `docs/style-previews/`（`blue-v3-hairline-preview.html` 为 3.0 定稿预览）。

## 测试

- 后端：JUnit 5 + Spring Boot Test + spring-security-test，测试位于 `dms-backend/src/test/java`，包结构与主代码镜像（含 `db/SchemaConstraintTest` 数据库约束测试）。运行 `cd dms-backend && mvn test`。
- 前端：无单元测试，以 `npm run build`（含 vue-tsc 类型检查）为门槛。
- `docs/testing/` 内有水电结算业务的手工/SQL 冒烟测试文档。
- CI（`.github/workflows/ci.yml`，push/PR 到 main 触发）：前端 `npm ci && npm run build`；后端 `mvn --batch-mode test`。提交前本地应能通过这两项。

## 数据库与迁移

- 本地/测试：H2，`db/schema.sql`（建表）+ `db/data.sql`（种子数据）。
- 生产（`prod` profile，`application-prod.yml`）：MySQL 8 + Flyway，迁移目录 `db/migration/V*.sql`，`baseline-on-migrate: true`（既有库以 V1 为基线）。
- **重要**：不要修改已发布的 `V*.sql`，也不要直接改生产库；新增结构变更一律新建递增版本文件（如 `V10__xxx.sql`）。`db/schema.sql` 与最新迁移结果需保持一致。
- 详细部署与迁移流程见 `docs/DEPLOY.md`。

## 部署

- 日常部署（服务器）：
  ```bash
  git pull --ff-only origin main
  docker compose up -d --build
  ```
- CD（`.github/workflows/deploy.yml`）：GitHub Actions 手动触发的 SSH 部署，secrets 见 `docs/DEPLOY.md`。
- 后端容器以 `SPRING_PROFILES_ACTIVE=prod` 启动；必需环境变量：`MYSQL_HOST/MYSQL_DATABASE/MYSQL_USER/MYSQL_PASSWORD`、`JWT_SECRET`、`INTEGRATION_TOKEN`（模板见 `.env.example`）。
- **禁止** `docker compose down -v`（会删除 MySQL 数据卷）。部署后检查 Flyway 历史与后端日志，流程见 `docs/DEPLOY.md`。

## 安全注意事项

- `application.yml` 中的 JWT secret 与 integration token 仅为本地演示值，生产必须通过环境变量覆盖（`JWT_SECRET`、`INTEGRATION_TOKEN`），`.env` 不入库。
- 密码一律 BCrypt 存储；登录接口 `/api/auth/login` 之外的业务接口均需 JWT。
- `/api/integration/**` 免 JWT 但必须校验 integration token。
- SQL 全部走 MyBatis-Plus / Mapper，不要拼接原生 SQL 字符串。
- 提交前确认不包含任何真实密码、密钥或服务器信息。
