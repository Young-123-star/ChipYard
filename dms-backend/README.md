# DMS 后端（DEMO）

公司宿舍管理系统 — 第一阶段 DEMO 后端。宿舍资源管理（楼栋/楼层/房间/床位）+ 房间状态看板 + 登录认证。

## 技术栈
Spring Boot 3.2 / JDK 17 · MyBatis-Plus · Spring Security + JWT · H2(内存库) · Knife4j

## 运行
```
mvn spring-boot:run        # 端口 8080，H2 内存库启动时自动建表 + 种子数据
```

## 接口文档（Knife4j）
http://localhost:8080/doc.html

## H2 控制台
http://localhost:8080/h2-console  （JDBC URL: `jdbc:h2:mem:dms`，用户 `sa`，空密码）

## 种子账号
admin / admin123

## 主要接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/login | 登录，返回 JWT |
| GET | /api/auth/me | 当前登录用户 |
| GET/POST/PUT/DELETE | /api/buildings | 楼栋 CRUD |
| GET/POST/PUT/DELETE | /api/floors?buildingId= | 楼层 CRUD |
| GET/POST/PUT/DELETE | /api/rooms | 房间 CRUD（多条件筛选）|
| GET | /api/rooms/board?buildingId= | 房间状态看板 |
| GET/POST/PUT/DELETE | /api/beds?roomId= | 床位 CRUD |

除 `/api/auth/login` 和文档/控制台路径外，其余接口需在请求头携带 `Authorization: Bearer <token>`。

## 测试
```
mvn test                   # 18 个测试（service/controller）
```

## 切换到 MySQL（后续）
1. `application.yml` 数据源改为 MySQL 连接；
2. 关闭 `spring.sql.init` 或改用 Flyway 管理建表脚本；
3. 把 `db/schema.sql` 迁移为 Flyway 版本化脚本；
4. `dms_room.facilities` 改回原生 JSON 列（当前 DEMO 以 VARCHAR 存 JSON 文本）。
