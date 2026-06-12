# 宿舍管理系统 DEMO — 开发进度

> 每次开发推进前先读本文件恢复上下文；推进结束前更新本文件。
> 设计文档：`docs/superpowers/specs/2026-06-11-宿舍管理系统DEMO-design.md`

## 当前阶段（2026-06-12）
**前端 DEMO 完成（实现 + E2E 联调 + Apple 风全局换肤）。分支 `feat/demo-frontend` 待推送 GitHub（用户用 GitHub Desktop 推）。**

### 数据展示增强 v2（2026-06-12，commit 67cbd2c，按用户确认的推荐组合实施）
- 看板：入住率总览条（入住率/空闲床位/已满/维修/房间/床位/地址）、图例可点击筛选（带计数）、**按楼层分组**（1F/2F 分段+每层小计）、房间格性别标识 ♂/♀
- 楼层页：房间数/床位数改为**后端实时统计**（不再用静态种子值）+ 入住进度条 + "查看房间"跳转（带 buildingId/floorId 路由参数）
- 房间页：行展开显示床位明细+面积/朝向、facilities JSON 解析为设施标签（空调/热水器/衣柜×2…）、"标记维修/恢复空闲"快捷切换、接收楼层页跳转参数
- 后端：RoomBoardVO + floorNumber/genderLimit；新增 FloorVO（roomCount/bedCount/occupiedBeds 实算）；测试 19 个全过（含楼层统计断言）
- 截图：docs/style-previews/v2-board.jpeg、v2-rooms-expand.jpeg
- 未做（记录在案）：看板悬停详情、入住人姓名（二期）、筛选汇总条、楼栋卡片化

### 视觉风格（已定）
- 出了 4 套风格小样（docs/style-previews/：A Apple 风 / B 暗色控制台 / C 极简办公 / D 建筑蓝图，含 html+jpeg）。
- 用户选定：**A·Apple 风全局** + 看板用 **D 蓝图的信息结构**（图签信息条、连体房间格、床位小图标、图例）以 Apple 材质渲染。
- 实施：`src/styles/theme.css`（全局令牌 + Element Plus 变量覆盖）、layout（毛玻璃浅色侧栏）、login（渐变+毛玻璃卡）、board（蓝图信息结构）。commit cf76eb2。
- 换肤后截图：docs/style-previews/applied-{board,buildings,login}.jpeg。浏览器验证零报错。

### 前端完成情况（feat/demo-frontend 分支）
- Task 1 脚手架（9ef898a + 4f55694）/ Task 2+3 API+store+路由（b5385ab）/ Task 4 登录+外壳（b74aeea）
- Task 5–9 五页（800fbec/6ca84f8/415d93b/f21a0aa/a05fe7d）
- 浏览器 E2E 全绿：登录→楼栋/楼层/房间/床位（级联）/看板，控制台零报错；看板截图 docs/screenshot-board.png
- 联调发现并修复后端编码 Bug：Windows GBK 默认编码导致种子中文乱码 → application.yml 加 `spring.sql.init.encoding: UTF-8` + pom 加 `project.build.sourceEncoding=UTF-8`
- 已知小项：刷新后顶栏用户名回退显示 "admin"（userInfo 不持久化，仅显示问题）

### 运行方式
- 后端：`cd dms-backend && mvn spring-boot:run`（:8080）
- 前端：`cd dms-frontend && npm run dev`（:5173 代理 /api）；admin/admin123

### Git / GitHub 状态
- 分支 `feat/demo-backend` 已推送到远程 `origin`（https://github.com/Young-123-star/ChipYard.git）。
- 已 `git merge --allow-unrelated-histories origin/main`（并入 main 的 README，解决"无关历史"以便建 PR）。
- 用户用 GitHub Desktop 操作推送（CLI 推送会被防火墙/凭据拦）。PR 描述见 `docs/PR-后端DEMO.md`。
- 本机杂项（.claude/、_clean_*、_unpacked_*、*.docx、*.stackdump）已加入 .gitignore，不入库。
- 设计文档：`docs/superpowers/specs/2026-06-11-宿舍管理系统DEMO-design.md`
- 后端计划：`docs/superpowers/plans/2026-06-11-宿舍管理DEMO-后端.md`（Task 1–12）
- 前端计划：待后端 API 定型后编写

### 后端 Task 进度（commit）
- ✅ Task 1 脚手架 d8b7698
- ✅ Task 2 common 统一返回/异常 dfd5970
- ✅ Task 3 BaseEntity + MyBatis-Plus 配置 3a2ac41
- ✅ Task 4 安全/JWT 8ac45e9 + 修复 c649918（isValid 回归标准签名校验）
- ✅ Task 5 H2 schema + 种子数据 0426171（admin/admin123，BCrypt 已验证）
- ✅ Task 6 auth 登录/当前用户 494f10c（登录测试通过=哈希正确）
- ✅ Task 7 楼栋 CRUD 样板 83d352a（含软删过滤测试，已坐实逻辑删除+分页在 H2 正常）
- ✅ Task 8 楼层 CRUD 0c9873b
- ✅ Task 9 房间 CRUD（多条件筛选）7d190ec
- ✅ Task 10 床位 CRUD 5b83ddd
- ✅ Task 11 房间状态看板 070827c
- ✅ Task 12 README bd5e219

### 已解决的关注点
- 逻辑删除 @TableLogic(value="null", delval="now()") 在 H2 正常（Task 7 软删测试验证）
- DbType.MYSQL 分页在 H2 正常（Task 7/9 分页测试验证）

### 接口清单（前端计划据此编写）
- POST /api/auth/login, GET /api/auth/me
- /api/buildings (CRUD 分页), /api/floors?buildingId= (列表+CUD)
- /api/rooms (CRUD 分页+筛选), /api/rooms/board?buildingId= (看板), /api/beds?roomId= (列表+CUD)
- 统一返回 R{code,message,data}；非登录接口需 Bearer token

## 关键决策速览
- 后端 Spring Boot 3.2 / JDK 17 + MyBatis-Plus + Spring Security/JWT
- 前端 Vue3 + Vite + Element Plus + Pinia
- DEMO 用 H2 内存库 + 种子数据（不建 MySQL）
- 垂直切片：宿舍资源管理（楼栋→楼层→房间→床位 + 看板）+ 保留登录

## 切片进度
| # | 切片 | 状态 |
|---|---|---|
| 1 | 后端地基（common/H2/Knife4j） | ⬜ 待办 |
| 2 | 登录切片（JWT/Security） | ⬜ 待办 |
| 3 | 楼栋切片（样板） | ⬜ 待办 |
| 4 | 楼层切片 | ⬜ 待办 |
| 5 | 房间切片 | ⬜ 待办 |
| 6 | 床位切片 | ⬜ 待办 |
| 7 | 看板接口 | ⬜ 待办 |
| 8 | 前端地基 | ⬜ 待办 |
| 9 | 前端资源页 | ⬜ 待办 |
| 10 | 前端看板页 | ⬜ 待办 |
| 11 | 联调 + 种子数据 + README | ⬜ 待办 |

状态图例：⬜ 待办 / 🟡 进行中 / ✅ 已完成

## 下一步
用户复核设计文档；通过后用 writing-plans 生成详细实现计划，然后从切片 1 开始。

## 已知限制 / 后续可补（全局审查得出，DEMO 可接受）
- update 未做唯一性校验（create 已查重）：PUT 可改出重复 building_code/room_number/bed_number。后补或靠 DB 唯一约束兜底。
- schema.sql 无 DB 级唯一约束/外键；切 MySQL 时补（building_code、building_id+room_number、room_id+bed_number）。
- GlobalExceptionHandler.handleOther 直接返回 e.getMessage()，生产环境应改为通用提示。
- 冗余统计字段（total_rooms/occupied_beds 等）为种子静态值，服务层不维护——前端勿当实时值展示。
- 分页 size 无上限；看板返回全部不分页。DEMO 可接受。

## 未决问题 / 备注
- 前端实现计划待编写（接口清单见上）。

## 变更日志
- 2026-06-11 完成头脑风暴与设计文档，建立本进度文件。
- 2026-06-11 后端 DEMO 全部 12 个 Task 完成（子 Agent 驱动 + 两阶段审查），18 测试通过；全局审查通过。
- 2026-06-11 写好前端实现计划；推送 feat/demo-backend 到 GitHub，准备 PR；收工。下次执行前端计划。
