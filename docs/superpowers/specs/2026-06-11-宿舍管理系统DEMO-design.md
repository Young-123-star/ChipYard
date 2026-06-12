# 公司宿舍管理系统 — 第一阶段 DEMO 设计文档

- 日期：2026-06-11
- 状态：已确认（待用户复核）
- 范围：第一阶段核心之 DEMO —— 宿舍资源管理垂直切片 + 登录

---

## 1. 背景与目标

公司需要一套覆盖宿舍分配、入住、费用、维修、安检的数字化管理系统（完整需求见 `_clean_req.txt`，原始数据库设计见 `_clean_db.txt`）。

本期目标是**先产出一个可运行的 DEMO**：不搭建外部数据库，前后端均为真实代码，跑通"宿舍资源管理"一条完整垂直切片，作为后续模块复制扩展的样板。

**DEMO 成功标准：**
- 后端 `mvn spring-boot:run` 零外部依赖直接启动（H2 内存库自动建表 + 种子数据）。
- 前端 `npm run dev` 启动，用种子 admin 账号登录后进入管理后台。
- 可对楼栋 / 楼层 / 房间 / 床位做增删改查，并查看房间状态看板。
- 代码是"数据库就绪"的真实代码，未来切换 MySQL 仅需改配置 + 迁移建表脚本，几乎零返工。

## 2. 技术栈

| 层 | 选型 | 备注 |
|---|---|---|
| 后端框架 | Spring Boot 3.2.x / JDK 17 | LTS |
| ORM | MyBatis-Plus 3.5.x | 贴合现有 SQL、代码生成友好 |
| 认证授权 | Spring Security + JWT | 无状态认证 |
| DEMO 数据库 | H2（内存模式，MySQL 兼容方言） | 启动加载 schema.sql + data.sql |
| 接口文档 | Knife4j (SpringDoc OpenAPI) | `/doc.html` 在线调试 |
| 参数校验 | Jakarta Validation | |
| 前端 | Vue3 + Vite + Element Plus + Pinia + Vue Router + Axios | 中后台标准组合 |

> 第一阶段正式版（接 MySQL 后）将补回：Flyway 数据库迁移、Redis 缓存、MinIO 文件存储、Docker 部署。DEMO 阶段刻意省略以保持轻量。

## 3. 整体架构

```
Vue3 + Element Plus (Vite, :5173)
        │  HTTP/JSON, /api 经 Vite proxy 转发
        ▼
Spring Boot 模块化单体 (:8080)
  common（统一返回/异常/分页/安全/MyBatisPlus 配置）
  module/auth（登录、当前用户）
  module/resource（楼栋/楼层/房间/床位/看板）
        │
        ▼
H2 内存库（启动时 schema.sql 建表 + data.sql 种子数据）
```

设计原则（适配 Claude Code 逐片开发）：
- **Package-by-Feature**：按业务模块分包，每模块自带 controller/service/mapper/entity/dto/vo。
- **模块隔离**：模块间只通过对方 Service 接口调用，不跨模块直接访问 mapper。
- **小而专注的文件**：单文件单一职责，便于 AI 在有限上下文内稳定改动。
- **每个切片可独立验证**：后端能起 + 测试能过；前端页面能点。

## 4. 后端工程结构

```
dms-backend/
├── pom.xml
├── src/main/java/com/company/dms/
│   ├── DmsApplication.java
│   ├── common/
│   │   ├── result/        # R<T> 统一返回、PageResult 分页结果、ResultCode 枚举
│   │   ├── exception/     # GlobalExceptionHandler、BizException
│   │   ├── security/      # JwtUtil、SecurityConfig、JwtAuthFilter、@CurrentUser
│   │   └── mybatis/       # MyBatisPlusConfig（分页插件）、MetaObjectHandler（字段自动填充）、逻辑删除配置
│   └── module/
│       ├── auth/
│       │   ├── controller/AuthController.java
│       │   ├── service/AuthService(.java/Impl)
│       │   └── dto/LoginRequest.java, vo/LoginVO.java, vo/CurrentUserVO.java
│       └── resource/
│           ├── controller/  BuildingController / FloorController / RoomController / BedController / RoomBoardController
│           ├── service/     （接口 + Impl，按上述实体各一套）
│           ├── mapper/      BuildingMapper / FloorMapper / RoomMapper / BedMapper（继承 BaseMapper）
│           ├── entity/      User / Building / Floor / Room / Bed
│           ├── dto/         查询/新增/修改入参
│           └── vo/          列表/详情/看板出参
├── src/main/resources/
│   ├── application.yml      # 数据源指向 H2 内存库；JWT 密钥/过期；MyBatisPlus 配置
│   ├── db/schema.sql        # H2 建表
│   └── db/data.sql          # 种子数据
└── src/test/java/com/company/dms/
    └── module/...           # 与主代码镜像的测试（Service 单测 + Controller MockMvc 测试）
```

## 5. 数据模型（DEMO 范围，重新梳理）

通用字段（所有 dms_ 表）：`created_at`、`updated_at`、`deleted_at`（逻辑删除）。状态字段用 TINYINT。

### 5.1 sys_user（仅登录用，DEMO 种子一条 admin）
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK 自增 | |
| username | VARCHAR(50) 唯一 | 登录账号 |
| password | VARCHAR(100) | BCrypt 加密 |
| real_name | VARCHAR(50) | 真实姓名 |
| gender | TINYINT | 0未知 1男 2女 |
| status | TINYINT | 0禁用 1启用 |
| created_at / updated_at / deleted_at | DATETIME | |

### 5.2 dms_building 楼栋
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| building_code | VARCHAR(50) 唯一 | 楼栋编码 |
| building_name | VARCHAR(100) | 楼栋名称 |
| address | VARCHAR(255) | 地址 |
| floor_count | INT | 总楼层数 |
| has_elevator | TINYINT | 0否 1是 |
| total_rooms | INT | 房间总数（统计冗余）|
| total_beds | INT | 床位总数（统计冗余）|
| status | TINYINT | 0停用 1启用 2维修中 |
| remark | VARCHAR(500) | |
| 通用字段 | | |

### 5.3 dms_floor 楼层
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| building_id | BIGINT | 所属楼栋，唯一键(building_id, floor_number) |
| floor_number | INT | 楼层号 |
| floor_name | VARCHAR(50) | 楼层名称 |
| room_count | INT | 房间数（统计冗余）|
| bed_count | INT | 床位数（统计冗余）|
| status | TINYINT | 0停用 1启用 |
| 通用字段 | | |

### 5.4 dms_room 房间
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| building_id | BIGINT | 所属楼栋 |
| floor_id | BIGINT | 所属楼层，唯一键(building_id, room_number) |
| room_number | VARCHAR(20) | 房间号 |
| room_type | TINYINT | 1单 2双 3四 4六 5八 6其他 |
| area | DECIMAL(6,2) | 面积 m² |
| orientation | VARCHAR(20) | 朝向 |
| bed_count | INT | 床位数量 |
| occupied_beds | INT | 已入住床位数 |
| facilities | JSON | 如 {"air_conditioner":1,"water_heater":1,"wardrobe":2,"desk":2} |
| gender_limit | TINYINT | 0不限 1男 2女 |
| status | TINYINT | 0停用 1空闲 2已满 3维修中 4预留 |
| remark | VARCHAR(500) | |
| 通用字段 | | |

### 5.5 dms_bed 床位
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| room_id | BIGINT | 所属房间，唯一键(room_id, bed_number) |
| bed_number | VARCHAR(20) | 床位编号（A/B/1/2）|
| bed_type | TINYINT | 1上床 2下床 3单床 |
| current_user_id | BIGINT | 当前入住人（DEMO 多为空）|
| status | TINYINT | 0停用 1空闲 2已入住 3维修中 4预留 |
| 通用字段 | | |

> JSON 字段在 H2 中以 `VARCHAR/CLOB` 存储，MyBatis-Plus 用 JacksonTypeHandler 映射；切 MySQL 时改回原生 JSON 列即可。

## 6. API 设计

- 路径前缀 `/api`，统一返回体 `R<T>` = `{ code, message, data }`；分页 data = `{ records, total, page, size }`。
- 认证：除 `/api/auth/login`、`/doc.html` 等放行外，其余需 `Authorization: Bearer <token>`。

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/login | 登录，返回 JWT + 用户信息 |
| GET | /api/auth/me | 当前登录用户信息 |
| GET | /api/buildings | 楼栋分页列表（支持名称/状态筛选）|
| POST/PUT/DELETE | /api/buildings(/{id}) | 楼栋增/改/删（软删）|
| GET | /api/floors?buildingId= | 某楼栋楼层列表 |
| POST/PUT/DELETE | /api/floors(/{id}) | 楼层增/改/删 |
| GET | /api/rooms | 房间分页列表（按楼栋/楼层/房型/状态筛选）|
| POST/PUT/DELETE | /api/rooms(/{id}) | 房间增/改/删 |
| GET | /api/beds?roomId= | 某房间床位列表 |
| POST/PUT/DELETE | /api/beds(/{id}) | 床位增/改/删 |
| GET | /api/rooms/board?buildingId=&floorId= | 房间状态看板聚合数据 |

错误处理：BizException → 业务错误码 + 提示；参数校验失败 → 400 + 字段级提示；未认证 → 401；无权限 → 403。

## 7. 前端工程结构

```
dms-frontend/
├── vite.config.ts          # dev server proxy: /api → http://localhost:8080
├── src/
│   ├── main.ts, App.vue
│   ├── api/                # auth.ts / building.ts / floor.ts / room.ts / bed.ts
│   ├── router/             # 路由表 + 登录守卫（无 token 跳登录）
│   ├── stores/             # user(token,userInfo) / app
│   ├── layout/             # 后台外壳：侧边栏菜单 + 顶栏 + 面包屑 + 退出
│   ├── views/
│   │   ├── login/
│   │   └── resource/       # building / floor / room / bed / board
│   ├── components/         # ProTable(分页表格) / SearchBar / StatusTag / 表单弹窗
│   └── utils/              # request.ts(axios 拦截器/统一错误提示) / dict.ts(房型/状态等常量)
```

UI 约定：列表页 = 搜索栏 + 表格 + 分页 + 新增/编辑弹窗；状态用彩色 Tag；看板用网格卡片按状态着色。

## 8. 测试策略

- 后端 Service 层关键逻辑写单元测试；Controller 用 MockMvc 测试主路径与校验失败路径。
- 采用 TDD：每个切片先写测试（红）→ 实现（绿）→ 重构。
- DEMO 不追求覆盖率指标，但"楼栋切片"作为样板要测试齐全，供后续模块照抄。
- 前端以手动点测为主（DEMO 阶段）。

## 9. 运行方式

```
# 后端
cd dms-backend && mvn spring-boot:run        # H2 自动建表+种子数据，:8080，文档 /doc.html

# 前端
cd dms-frontend && npm install && npm run dev  # :5173，代理 /api 到 :8080
```

种子 admin 账号（在 data.sql / README 注明），登录后进入资源管理。

## 10. 任务拆分（垂直切片）

按以下顺序实现，每片是一个可验证的小闭环：

1. **后端地基**：pom + 骨架 + common（R/异常/分页/MyBatisPlus 配置/字段填充）+ H2 配置 + Knife4j。
2. **登录切片**：sys_user 实体/mapper + JWT + Security + 登录/当前用户接口 + 测试。
3. **楼栋切片**（样板）：entity→mapper→service→controller→单测，一条龙打样。
4. **楼层切片**：依赖楼栋。
5. **房间切片**：含 facilities JSON、状态、多条件筛选。
6. **床位切片**：依赖房间。
7. **看板接口**：聚合房间状态。
8. **前端地基**：Vite 工程 + axios 封装 + 路由守卫 + Pinia + layout + 登录页。
9. **前端资源页**：楼栋/楼层/房间/床位 CRUD（复用通用表格组件）。
10. **前端看板页**：房间状态网格视图。
11. **联调 + 种子数据完善 + README**。

## 11. 进度管理约定

- 项目内维护 `docs/PROGRESS.md`，记录各切片状态、当前步骤、下一步、关键决策与未决问题。
- 每次开发推进结束前更新 PROGRESS.md；下次开始前先读它恢复上下文。

## 12. 非目标（本期不做）

入住/退宿/费用/维修/巡检/访客/通知/违规等模块、完整 RBAC 权限管理、移动端、MySQL/Redis/MinIO/Flyway/Docker、文件上传、第三方系统对接。这些在 DEMO 跑通后按阶段推进。
