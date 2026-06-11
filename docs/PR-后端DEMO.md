# PR 描述 — 宿舍管理系统 第一阶段 DEMO 后端（资源管理 + 登录）

> 用于 `feat/demo-backend` → `main` 的合并请求描述。

## 概述

公司宿舍管理系统 —— **第一阶段 DEMO 后端**。实现宿舍资源管理的完整后端：登录认证 + 楼栋/楼层/房间/床位 CRUD + 房间状态看板。零外部依赖即可启动（H2 内存库 + 种子数据），代码保持"数据库就绪"，后续切换 MySQL 几乎零返工。

## 技术栈

Spring Boot 3.2 / JDK 17 · MyBatis-Plus 3.5 · Spring Security + JWT · H2(内存库) · Knife4j · 模块化单体（package-by-feature）

## 本次包含的功能

- **认证**：登录（JWT）、获取当前用户；除登录/文档外的接口统一鉴权
- **公共基础**：统一返回体 `R{code,message,data}`、分页封装、全局异常处理、操作字段自动填充、逻辑删除（软删除）
- **宿舍资源管理**
  - 楼栋：分页列表 + 增/改/删（含编码查重、软删除）
  - 楼层：按楼栋查询 + 增/改/删
  - 房间：分页 + 多条件筛选（楼栋/楼层/房型/状态）+ 增/改/删
  - 床位：按房间查询 + 增/改/删
  - 房间状态看板：按楼栋/楼层聚合房间占用状态

## 主要接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/login | 登录，返回 JWT |
| GET | /api/auth/me | 当前登录用户 |
| GET/POST/PUT/DELETE | /api/buildings | 楼栋（分页 CRUD）|
| GET/POST/PUT/DELETE | /api/floors?buildingId= | 楼层 |
| GET/POST/PUT/DELETE | /api/rooms | 房间（筛选 + CRUD）|
| GET | /api/rooms/board?buildingId= | 房间状态看板 |
| GET/POST/PUT/DELETE | /api/beds?roomId= | 床位 |

## 如何运行

```
cd dms-backend
mvn spring-boot:run
```

- 服务端口 8080，H2 启动时自动建表 + 种子数据
- 接口文档：http://localhost:8080/doc.html
- 演示账号：`admin / admin123`

## 测试

`mvn test` —— **18 个测试全部通过**，覆盖：JWT 签名/防篡改、登录成功/失败、未授权访问(401)、各 CRUD 主路径、参数校验失败、软删除过滤、分页与多条件筛选。

## 文档

- 设计文档：`docs/superpowers/specs/2026-06-11-宿舍管理系统DEMO-design.md`
- 后端实现计划：`docs/superpowers/plans/2026-06-11-宿舍管理DEMO-后端.md`
- 进度记录：`docs/PROGRESS.md`

## 已知限制（DEMO 范围内，后续迭代处理）

- 更新接口未做唯一性校验（新增已查重）；DB 层暂无唯一约束/外键
- 全局异常处理直接返回异常信息，生产环境需收敛为通用提示
- 冗余统计字段（total_rooms/occupied_beds 等）为种子静态值，服务层暂不维护
- 分页 size 无上限；看板返回全部不分页

## 不在本次范围

前端（另起实现）、入住/退宿/费用/维修/巡检/访客等模块、完整 RBAC、移动端、MySQL/Redis/MinIO/Flyway/Docker、第三方系统对接 —— 均为后续阶段。
