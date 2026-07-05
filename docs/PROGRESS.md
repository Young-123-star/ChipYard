# 宿舍管理系统 DEMO — 开发进度

> 每次开发推进前先读本文件恢复上下文；推进结束前更新本文件。
> 设计文档：`docs/superpowers/specs/2026-06-11-宿舍管理系统DEMO-design.md`

## 当前阶段（2026-07-05）
**项目 DEMO 已部署到服务器，当前进入生产化收敛阶段。** README 仅临时记录过 2026-07-03 的部署安全化改造；README 暂不作为开发流水账，后续仍以本文件恢复上下文。

### 已部署/版本基线
- 本地 `main` 跟踪 `origin/main`，当前工作区干净。
- 最近部署安全化改造：`c9dc615 chore: harden docker database deployment 20260703`。
- 前端版本：`dms-frontend/package.json` = `0.0.1`。
- 后端版本：`dms-backend/pom.xml` = `0.0.1-SNAPSHOT`。
- 生产部署方式：Docker Compose + MySQL 8；生产数据库迁移由 Flyway 管理，详见 `docs/DEPLOY.md`。

### 2026-07-03 部署安全化改造（已完成）
- 背景：项目已部署到服务器，生产 MySQL 会产生真实数据，不能继续依赖 `SQL_INIT_MODE=always` 反复执行 `schema.sql/data.sql`。
- 生产 `prod` 环境关闭 Spring SQL 初始化：`spring.sql.init.mode=never`。
- 生产 `prod` 环境启用 Flyway：`classpath:db/migration`，并开启 `baseline-on-migrate` 兼容服务器已有库。
- 新增生产迁移脚本：`V1__init_schema.sql`、`V2__demo_seed_data.sql`。
- 从 `docker-compose.yml` / `.env.example` 移除 `SQL_INIT_MODE`。
- 新增部署文档：`docs/DEPLOY.md`；明确不要执行 `docker compose down -v`。
- 验证记录：后端 `mvn test`，共 120 个测试通过。

### 维修工单管理（文档显示已完成，需纳入当前基线）
- 设计/计划：`docs/superpowers/specs/2026-06-17-维修工单管理-design.md`、`docs/superpowers/plans/2026-06-17-维修工单管理.md`。
- PR 文档：`docs/PR-维修工单管理.md`。
- 范围：新增后端 `repair` 模块；新增 `dms_repair_order` 表和 3 条演示种子；前端新增「维修管理 / 维修工单」页面；支持建单、列表、详情、受理、完成、取消。
- 验证记录：后端 `mvn test` 118 tests passed；前端 `npm run build` 通过。
- 非目标：不联动房间状态，不生成维修费用账单，不接 OA/webhook 或居住人自助端。

### 2026-07-05 生产化收敛（已完成）
- 完善本进度文档，补齐部署后基线与维修工单状态。
- 全局未知异常提示收敛：兜底异常不再把内部 `e.getMessage()` 返回前端，统一返回 `ResultCode.SYSTEM_ERROR`；业务异常、参数校验等用户可见提示保持原逻辑。
- DB 核心唯一约束：本地 H2 `schema.sql` 同步约束；生产新增 Flyway `V3__resource_active_unique_constraints.sql`，覆盖 `building_code`、`building_id + room_number`、`room_id + bed_number`。
- 验证：后端 `mvn test` 通过，123 tests passed。
- 远程同步：本地 `main` 与 `origin/main` 均指向 `28fb57c chore: production hardening and DB constraints`。
- 服务器部署：`docker compose up -d --build` 后 `mysql` healthy，`backend`/`frontend` 均 Up；后端以 `prod` profile 启动。
- Flyway 线上状态：`V3__resource_active_unique_constraints.sql` 已成功执行，`flyway_schema_history` 显示 version 3 `resource active unique constraints` success=1。
- 页面冒烟：用户已验证页面正常。
- 暂不改 README；README 等项目进入相对稳定版本后再整理为项目入口页。

### 2026-07-05 部署后安全小修（已完成并部署）
- 处理 Spring Security generated password warning：新增显式 `UserDetailsService` bean，不提供默认用户；项目登录仍走既有 `/api/auth/login` + JWT 流程。
- 生产密钥配置收敛：`application-prod.yml` 移除 `JWT_SECRET` / `INTEGRATION_TOKEN` 默认 fallback，生产环境必须由服务器环境变量显式提供。
- 验证：`SecurityConfigTest`、`AuthControllerTest`、`JwtUtilTest` 通过；后端全量 `mvn test` 通过，124 tests passed；测试启动日志不再出现 generated password warning。
- 部署验证：服务器 `.env` 已确认有非空 `JWT_SECRET` 和 `INTEGRATION_TOKEN`；通过 SSH remote 拉取远程后 `docker compose up -d --build` 重建成功。
- 日志验证：后端 `prod` profile 正常启动，Flyway schema version 3 已是最新；`Using generated security password` warning 已消失。
- 页面冒烟：用户已验证登录和页面正常。

### 下一步候选（不发散）
- 下一步可进入小闭环业务增强：维修工单联动房间「维修中」状态；维修费用、SLA、维修人员实体、报修端继续后置。
- 报表增强优先级靠后；如做，先做 CSV 导出，Excel/缓存/同比环比暂不做。

## 当前阶段（2026-06-17 收工）
**第二阶段第六个子项目「账单报表/用量趋势」已全部实现完成，分支 `feat/report-stats`（从最新 main 切出，8 个 Task + TDD，子 Agent 驱动逐 Task 实现+两阶段审查+最终整体审查）。后端全量回归 111 测试全过，前端 `vue-tsc --noEmit` 零错误。待用户用 GitHub Desktop 推送建 PR（基于 main，无合并顺序约束）。** 水电费抄表计费（PR #9）、退宿欠费挂账（PR #8）、费用管理（PR #7）、退宿管理（PR #6）、入住管理（PR #5）、蓝色主题（PR #4）均已合并入 main。

### 账单报表/用量趋势子项目（已实现完成，2026-06-17，分支 feat/report-stats）
- 设计/计划：`docs/superpowers/specs/2026-06-17-账单报表用量趋势-design.md`、`docs/superpowers/plans/2026-06-17-账单报表用量趋势.md`
- 只读统计模块：对 `dms_fee_bill` + `dms_meter_reading` 做内存聚合，**不新增表、纯读**。`module/fee` 下新增 `ReportService`/`ReportServiceImpl`/`ReportController` + 4 个 VO，名称回显复用 `RoomService`/`BuildingService`/`ResidentService`。
- 四个接口（均 GET、JWT、挂 `/api/report`）：`period-summary`（按账期，住宿/电/水拆分+收缴率）、`building-summary`（按楼栋）、`arrears-ranking?limit=10`（欠费排行）、`usage-trend`（水电用量趋势）。
- 统一口径：应收=非作废账单(status≠3)；已缴=status2；未缴=status1(未缴)+4(挂账)；收缴率=已缴×100/应收（2 位 HALF_UP，应收0→0）；billType 2电/3水/其余住宿。全程 `BigDecimal`，`TreeMap` 实现 period/buildingId 升序。
- 前端：引入 `echarts`+`vue-echarts`（`main.ts` 按需注册 + 全局 `<v-chart>`）；新增菜单组「统计报表」+ `views/report/index.vue` 一页（账期组合图柱+收缴率双轴 / 用量趋势折线 / 楼栋横向柱 / 欠费 Top10 表）；`api/report.ts` + types 加 4 VO 接口。
- 后端测试：ReportServiceTest（账期拆分+收缴率+作废不计+未缴含挂账+total0→0+升序、楼栋 delta 断言+名称回显、欠费分组+降序+limit、用量按表型分账期合计）、ReportControllerTest（四接口+鉴权401），全量回归 **111 测试全过**。前端 `vue-tsc --noEmit` 零错误。
- 已知（非本次范围）：`npm run build`（vue-tsc -b）因 **既有** `vite.config.ts` 引 `node:url` 而项目未装 `@types/node` 报错（main 上同样存在，与本功能无关，沿用既有 `vue-tsc --noEmit` 门禁）。
- 非目标（后续）：报表导出 CSV/Excel、按房间/楼层下钻、自定义时间范围、同比/环比、定时快照/缓存。

### 水电费抄表计费子项目（已实现完成，2026-06-17，分支 feat/utility-billing）
- 设计/计划：`docs/superpowers/specs/2026-06-17-水电费抄表计费-design.md`、`docs/superpowers/plans/2026-06-17-水电费抄表计费.md`
- 按房间抄水/电表 → 用量×全局单价算费 → 按房间在住人均摊（余数并入第一张）→ 复用 `dms_fee_bill`（加 `bill_type` 1住宿费/2电费/3水费 + `remark`）开账单，自动走缴费/退宿挂账/欠费预览。
- 新增 2 表：`dms_meter_reading`（抄表台账，幂等键 room+period+type）/`dms_utility_rate`（单行单价配置）+ 种子（电1.00/水5.00 + A102 房 2026-06 电30/水40 抄表）。
- 两步流程：`MeterService.saveReading`（自动取上期读数、算用量金额、幂等 upsert）→ `generateUtilityBills`（`@Transactional` 按在住人均摊建单）。
- 衔接：`getByRecordAndPeriod` 加 billType 参数、住宿费 generate 限定 bill_type=1 防串味；`CheckinService.listActiveRecordsByRoom`；账单列表加 billType 筛选。`createUtilityBill` 落 UBILL-E/W 单号。
- 前端：「费用管理」加「抄表/水电」页（单价配置 + 楼栋→房间联动录入 + 台账表 + 生成账单）；账单页加类型列/筛选 + 明细列。
- 后端测试：FeeBillType（类型隔离）/MeterService（抄表自动算量幂等）/UtilityBillGenerate（均摊+余数+无在住跳过+幂等）/MeterController（接口鉴权）/CheckinActiveByRoom，全量回归 **100 测试全过**。
- Live 联调（curl，admin/admin123）：单价/台账/生成(2·0)/电费账单回显 remark「电费 30.00度×1.00 ÷1人」/录入 2026-07/幂等重生(0·2)，全通过。
- 非目标（后续）：阶梯水电价、按期可变单价、整栋总表分摊、滞纳金、用量趋势图。

### 退宿欠费结算挂账子项目（已实现完成，2026-06-16，分支 feat/checkout-arrears）
- 设计/计划：`docs/superpowers/specs/2026-06-16-退宿欠费结算挂账-design.md`、`docs/superpowers/plans/2026-06-16-退宿欠费结算挂账.md`
- 把费用与退宿打通：办理退宿（`CheckoutServiceImpl.confirm`，同一 `@Transactional`）末尾自动「欠费结算挂账」——该在住档案未缴账单置「挂账(4)」，退宿单回填「离场欠费金额」，退宿**不阻断**（挂账放行）。
- 数据改动仅 2 处：`dms_checkout_order` 加 `arrears_amount DECIMAL(10,2) DEFAULT 0`；账单状态新增取值 **4=挂账**（无新表）。
- fee 模块新增 `listUnpaidByRecord` / `settleArrearsForRecord`；`pay` 守卫放宽为「未缴(1)或挂账(4)可缴」（挂账可后续结清→已缴）；新增只读 `GET /api/fee/arrears?checkinRecordId=`（{count,totalAmount} 预览）。
- checkout 模块：`CheckoutOrder`/`CheckoutOrderVO` 加 `arrearsAmount`；confirm 注入 `FeeBillService` 结算。模块依赖方向 checkout→fee（无环）。
- 前端：退宿单「办理退宿」弹窗欠费预览（alert）、列表加「离场欠费」列；账单页 `BILL_STATUS` 加「挂账」可筛选、挂账账单仍可缴费。
- 后端测试：FeeArrearsService(listUnpaid/settle/pay放宽)、FeeArrearsController(arrears 接口+鉴权)、CheckoutArrears(confirm 集成)，全量回归 **83 测试全过**。
- Live 联调（curl，admin/admin123）：欠费预览(1/800)→办理退宿→退宿单 arrearsAmount=800.00+status=2→账单 status=4 挂账→挂账缴费→预览归 0，全通过。
- 非目标（后续）：滞纳金、部分缴费、坏账核销、催缴通知、账龄报表、退宿硬拦截。

### 费用管理子项目（已实现完成，2026-06-16，分支 feat/fee-module）
- 设计/计划：`docs/superpowers/specs/2026-06-16-费用管理子项目-design.md`、`docs/superpowers/plans/2026-06-16-费用管理子项目.md`
- 新增 `fee` 模块（entity/mapper/dto/vo/service/controller）：收费标准 `FeeStandardService` CRUD（房型唯一）+ 账单 `FeeBillService`（生成/缴费/作废/列表）。
- 新增 2 表：`dms_fee_standard`（room_type→月单价）/`dms_fee_bill`（账单，关联在住档案 + 金额快照）+ 种子（3 收费标准 + 张三 2026-06 账单）。
- CheckinService 加 `listActiveRecords()`（列出在住档案，供账单生成遍历）。
- 核心流程：选账期一键生成账单（`@Transactional`，对所有在住档案各生成一张、金额=房型月单价快照、`(档案,账期)` 幂等跳过、无标准跳过并回报 `{generated,skipped}`）；缴费（未缴→已缴+时间/方式，非未缴拒绝）；作废（未缴→作废）；欠费=列表按未缴筛选。
- 前端新增菜单组「费用管理」：收费标准页（增改删）/ 住宿费账单页（账期/状态筛选 + 汇总条 + 生成账单 + 缴费现金/转账 + 作废）。
- 后端测试：CheckinListActive/FeeStandardService(CRUD查重)/FeeBillService(生成幂等+快照+跳过/缴费/作废状态守卫)/FeeController(鉴权)，全量回归 **75 测试全过**。
- Live 联调（curl，admin/admin123）：收费标准列表(中文)/生成 2026-07(1/0)/幂等重生(0/1)/账单列表(张三·A102·room_type2·800.00 回显)/缴费→已缴+paidAt/重复缴费拒绝(50000)/无token→401，全通过。中文编码正常。
- 非目标（后续）：水电抄表、押金、滞纳金/催缴、部分缴费、退宿欠费拦截/结算挂账、按天比例计费。
- 后续可做方向：水电费抄表计费 / 押金 / 退宿欠费结算挂账（打通退宿模块）。

### 退宿管理子项目（已实现完成，2026-06-16，分支 feat/checkout-module）
- 设计/计划：`docs/superpowers/specs/2026-06-15-退宿管理子项目-design.md`、`docs/superpowers/plans/2026-06-15-退宿管理子项目.md`
- 新增 `checkout` 模块（entity/mapper/dto/vo/service/controller）+ 扩 `integration/oa`（两个 webhook + 两个适配器）+ 扩 `checkin`/`resident` 服务方法。
- 新增 1 表 `dms_checkout_order` + `dms_checkin_record` 加 `checkout_date` 列 + 一条待退宿单种子（SEED-CO-1）。
- 关键修复：`Bed.currentUserId` 加 `@TableField(updateStrategy=FieldStrategy.ALWAYS)`，使 `release` 置 null 能持久化（入住遗留 Bug，已 live 验证 bed3 退宿后 current_user_id=null）。
- 两个 OA webhook（`/api/integration/oa/checkout-application`、`/resignation`，需 `X-Integration-Token`，按 bizNo 幂等）匹配级联四 outcome：NO_RESIDENT / NO_ACTIVE_CHECKIN / RESIGNED_NO_CHECKIN（离职无在住置离职不建单）/ ORDER_CREATED（离职额外置居住人离职）。
- 办理退宿 `confirm`（单 `@Transactional`）：释放床位 + 刷新房间统计 + 在住档案置已退宿+回填 checkout_date + 退宿单置已退宿。
- 前端「入住管理」菜单组加「退宿单」页（列表/筛选/手工新建/办理退宿/取消）；types/dict/api/checkout 已加；vue-tsc 通过。
- 后端测试：BedRelease/CheckinRecordQuery/ResidentResign/CheckoutOrderService/CheckoutConfirmService/CheckoutController/OaCheckoutWebhook，全量回归 **62 测试全过**。
- Live 联调（curl，admin/admin123）：4 种 webhook outcome + 幂等 + 错误 token(40100) 全验证；登录→列退宿单→办理退宿→单变已退宿、bed3 释放(current_user_id=null/status=1) 全通过。中文编码正常。
- 未做（依赖费用子项目，非目标）：费用结算/退宿检查/损坏赔偿。
- 后续子项目：**费用**（水电费/账单/结算）。
- 待办：浏览器可视化 E2E + 截图未做（已 live curl 全覆盖同一流程）；用户 GitHub Desktop 推送建 PR。

### 下次开工第一步（退宿实现）
1. 读本文件 + `docs/superpowers/specs/2026-06-15-退宿管理子项目-design.md` + `docs/superpowers/plans/2026-06-15-退宿管理子项目.md` 恢复上下文。
2. 确认在分支 `feat/checkout-module`（已从最新 main 切出）。
3. 用 **superpowers:subagent-driven-development** 照计划逐个任务实现（12 个任务，TDD + 两阶段审查 + 最后整体审查），完成后端到端联调 + 更新本文件，再交用户 GitHub Desktop 推送建 PR。
4. 退宿后的下一个子项目：**费用**（水电费/账单/结算；退宿的费用结算挂账依赖它）。

### 退宿管理子项目（设计+计划已就绪，2026-06-15，分支 feat/checkout-module）
- 入口：OA **退宿申请单** + OA **离职单** 两个 webhook（复用 `X-Integration-Token`，按 biz_no 幂等）；保留手工新建。
- 匹配级联四种 outcome：无居住人→NO_RESIDENT「无居住记录」；离职无在住→置离职+RESIGNED_NO_CHECKIN（不建单）；退宿申请无在住→NO_ACTIVE_CHECKIN（拒绝）；在住→建待退宿单（离职额外置居住人离职）。
- 办理退宿（单事务）：释放床位 + 刷新房间统计 + 在住档案置已退宿+回填 checkout_date + 退宿单置已退宿。
- 关键技术点：修复 `Bed.currentUserId` 的 `@TableField(updateStrategy=ALWAYS)`（入住遗留占位）；CheckinService 加 getRecord/findActiveRecordByResident/markCheckedOut；ResidentService 加 markResigned。
- 新增 `checkout` 模块 + 扩 `integration/oa`；前端「入住管理」菜单组加「退宿单」页。
- 费用结算/退宿检查/损坏赔偿 = 非目标（依赖费用子项目）。

### 入住管理子项目（已合并 PR #5，2026-06-15，分支 feat/checkin-module，子 Agent 驱动 + 两阶段审查实现）
- 设计/计划：`docs/superpowers/specs/2026-06-15-入住管理子项目-design.md`、`docs/superpowers/plans/2026-06-15-入住管理子项目.md`
- 新增三模块：`resident`（居住人/员工档案）、`checkin`（意向单 intake + 入住档案 record）、`integration`（OA/HCP webhook 防腐层 + 出站客户端预留 + token 守卫）
- 新增 3 表：`dms_resident`/`dms_checkin_intake`/`dms_checkin_record`（schema+seed）
- 核心流程：Webhook/手工 → 意向单(待分配) → 管理员选床确认入住（@Transactional：性别校验+占床+刷新房间统计+生成档案+置已入住）→ 入住档案(在住)
- 对接预留：`POST /api/integration/oa/checkin-application`、`/hcp/employee`，需 `X-Integration-Token`（application.yml `integration.token`，DEMO 值 `dms-demo-integration-token`），按 bizNo/employee_no 幂等；真实 OA/HCP 细节到位只换适配器实现
- 前端新增菜单组「入住管理」：居住人管理 / 入住意向单（含楼栋→房间→空闲床位联动的分配弹窗）/ 入住档案
- 后端测试 41 个全过（resident CRUD/幂等、占床统计、assign 事务+性别校验、webhook 幂等+token、controller）
- 端到端联调：curl 投 OA/HCP 样例报文生成待分配意向单 → 浏览器分配入住 → 入住档案在住、床位占用。截图 docs/style-previews/checkin-{intakes,records}.jpeg
- 后续子项目：退宿（释放床位，BedService.release 已占位，需补 currentUserId 置 null 的 @TableField 策略）、费用

### 蓝色主题改版（已合并 PR #4，分支 feat/ui-blue-theme）
**前端 DEMO + 三轮数据/视觉增强已全部合并入 GitHub `main`（PR #1/#2/#3）。`feat/ui-blue-theme` 把全局视觉从 Apple 毛玻璃浅色风改为「蓝色企业主题 + 深蓝实色侧栏」。**

### 蓝色主题改版（2026-06-15，分支 feat/ui-blue-theme）
- 需求：用户给 P2P 系统参考图（`docs/style-previews/P2PStyle*.png`），要求主题色改蓝、左侧菜单改成（提亮后的）深蓝实色侧栏。
- 先出静态示例 `docs/style-previews/blue-theme-preview.html`（+ .jpeg）确认方向。
- 落地 3 处（保留所有 `--dms-*` 变量名，业务页零改动）：
  - `src/styles/theme.css`：令牌全面调色——主题蓝 `--dms-accent:#1f6feb`、冷调浅灰底、毛玻璃卡→干净白卡（去 backdrop-filter）、新增侧栏色板 `--dms-nav-*`（提亮版 top `#1a59ac`/bottom `#134890`、选中 `#2f80f7`）、Element Plus primary 变量改蓝、状态色微调。
  - `src/layout/index.vue`：侧栏深蓝实色渐变 + 白字、品牌加圆角 logo 块「C」、菜单按 `el-menu-item-group` 分「资源管理/视图」、选中项亮蓝填充块、顶栏纯白。
  - 侧栏亮度反复试了两版提亮（#1a59ac、#2569c8），**最终按用户要求回到初始深海军蓝** top `#0c2f63`/bottom `#0a2750`/选中 `#1f6feb`（即最初示例 blue-theme-preview 的配色）。
  - 登录页一度改为「左深蓝品牌栏 + 右白卡」分栏，**用户不满意，已回退为原 Apple 风**（字标渐变 + SVG 电路背景 + 毛玻璃卡，`login-bg.svg` 保留）；登录按钮随主题自然变蓝。
- 真实前端验证：登录→楼栋/楼层(表格)→看板，控制台零报错。截图 `docs/style-previews/blue-applied-{buildings,board}.jpeg`。
- P2P 参考图（`docs/style-previews/P2PStyle*.png`，10 张）已留档入库。
- 待办：用户用 GitHub Desktop 推送 `feat/ui-blue-theme` 并建 PR（CLI 推送被防火墙拦，`gh` 未装）。

### 合并状态（已全部进 main）
- PR #1：后端 DEMO（merge a0df189）
- PR #2：前端 DEMO（merge 4c2a074）
- PR #3：前端数据增强 v2/v3 + 品牌（merge eb3c81a）
- `origin/main` 最新 = eb3c81a。本地 `feat/demo-frontend` 工作已 100% 入 main，无未合并提交。

### 下次开工第一步
1. 读本文件恢复上下文。
2. 从最新 `main` 切新功能分支再开发（本地 main 已建好跟踪 origin/main）。
3. 启动服务验证：后端 `cd dms-backend && mvn spring-boot:run`（:8080），前端 `cd dms-frontend && npm run dev`（:5173）；admin/admin123。
4. 下一步方向未定（增强项已全部完成）。可选：第二阶段业务模块（入住/退宿/费用…，需先头脑风暴）、接 MySQL/Docker 部署、或继续打磨。开工时问用户。

### 已完成全景（feat/demo-frontend）
- 前端 DEMO 实现（脚手架→API/store/路由→登录+外壳→楼栋/楼层/房间/床位/看板），E2E 联调通过
- Apple 风全局主题；登录页重设计（芯片电路 SVG 背景）；品牌 **ChipMore Dorm**
- 数据增强 v2：看板入住率总览条/图例筛选/按楼层分组/性别标识；楼层实时统计+进度条+跳转；房间行展开床位/设施标签/快捷维修
- 数据增强 v3：看板悬停详情；房间筛选汇总条；楼栋卡片化+入住率环
- 后端配套小改：RoomBoardVO 扩字段、RoomSummaryVO+summary 接口、FloorVO/BuildingVO 实时统计、UTF-8 编码修复；**后端 19 测试全过**

### 交付物文档
- PR 文案：`docs/PR-前端DEMO.md`（前端）、`docs/PR-后端DEMO.md`（后端，已合并入 main）
- 设计/计划：`docs/superpowers/specs/` 与 `docs/superpowers/plans/`
- 效果截图：`docs/style-previews/`（applied-* / v2-* / v3-*）

### 数据展示增强 v2（2026-06-12，commit 67cbd2c，按用户确认的推荐组合实施）
- 看板：入住率总览条（入住率/空闲床位/已满/维修/房间/床位/地址）、图例可点击筛选（带计数）、**按楼层分组**（1F/2F 分段+每层小计）、房间格性别标识 ♂/♀
- 楼层页：房间数/床位数改为**后端实时统计**（不再用静态种子值）+ 入住进度条 + "查看房间"跳转（带 buildingId/floorId 路由参数）
- 房间页：行展开显示床位明细+面积/朝向、facilities JSON 解析为设施标签（空调/热水器/衣柜×2…）、"标记维修/恢复空闲"快捷切换、接收楼层页跳转参数
- 后端：RoomBoardVO + floorNumber/genderLimit；新增 FloorVO（roomCount/bedCount/occupiedBeds 实算）；测试 19 个全过（含楼层统计断言）
- 截图：docs/style-previews/v2-board.jpeg、v2-rooms-expand.jpeg
- 未做（记录在案）：入住人姓名（二期，依赖入住档案）

### 数据展示增强 v3（2026-06-12，commit f8b3e57）
- 看板：房间格**悬停弹出详情**（面积/朝向/床位/性别限制/设施）
- 房间页：**筛选汇总条**（共 X 间 · 床位 · 已住 · 空闲，覆盖全量不受分页限制）
- 楼栋页：表格→**卡片化**，仪表盘入住率环 + 楼层/房间/床位/空闲/电梯 + 查看房间跳转
- 后端：RoomBoardVO 增 area/orientation/facilities；新增 RoomSummaryVO + GET /api/rooms/summary；新增 BuildingVO（realRoomCount/realBedCount/occupiedBeds 实算），楼栋列表接口改返回 BuildingVO；19 测试仍全过
- 截图：docs/style-previews/v3-buildings.jpeg、v3-rooms.jpeg、v3-board-hover.jpeg

### 登录页重设计 + 品牌名（2026-06-12，commit bbc5bb1）
- 品牌名定为 **ChipMore Dorm**（演变：ChipYard Dram → Dorm → ChipMore Dorm）——登录页字标、侧栏 logo、浏览器标题三处生效。
- 登录页：左右分栏（左品牌字标+标语 / 右毛玻璃登录卡），背景为自制 SVG（`src/assets/login-bg.svg`：柔和渐变光斑 + 芯片电路走线意象，贴合 ChipYard 主题）。截图 docs/style-previews/v2-login.jpeg。

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
