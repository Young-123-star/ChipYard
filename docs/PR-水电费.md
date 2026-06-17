# PR 描述 — 水电费抄表计费

> 用于 `feat/utility-billing` → `main` 的合并请求。

## 标题

```
feat: 水电费抄表计费（按房间抄表 + 用量计费 + 按在住人均摊生成账单）
```

## 概述

第二阶段第五个子项目「水电费抄表计费」。在已合并的费用模块基础上，新增**按房间抄水/电表 → 用量×单价算费 → 按房间在住人均摊生成账单**。水电账单复用现有 `dms_fee_bill`（加 `bill_type` 区分），自动走既有的缴费 / 退宿挂账 / 欠费预览流程。全局单一单价，两步流程（先抄表台账，再按期生成账单）。仍沿用 H2 内存库 DEMO，TDD 逐 Task 实现。

设计与计划：`docs/superpowers/specs/2026-06-17-水电费抄表计费-design.md`、`docs/superpowers/plans/2026-06-17-水电费抄表计费.md`。

## 主要内容

### 后端
- **新增 2 表**：`dms_meter_reading`（抄表台账，幂等键 room+period+type，存上期/本期/用量/单价/金额快照）、`dms_utility_rate`（单行单价配置 电/水）。
- **`dms_fee_bill` 加 2 列**：`bill_type`（1住宿费/2电费/3水费，默认 1）+ `remark`（如「电费 30.00度×1.00 ÷2人」）。
- **`MeterService`**：
  - `getRate`/`updateRate` 单价配置读写。
  - `saveReading`：录入本期读数 → 自动取该房该表上一期读数、算用量×单价、幂等 upsert（room+period+type）。
  - `generateUtilityBills(period)`（`@Transactional`）：遍历该期台账，每间房费用按当前在住人**均摊**（HALF_UP 2 位，余数并入第一张，合计=房间总额），给每个在住居住人开账单；无在住跳过；按 `(档案,期,类型)` 幂等。
- **衔接现有费用体系**：`getByRecordAndPeriod` 加 `billType` 参数；住宿费 `generate` 判重/建单限定 `bill_type=1` 防串味；新增 `FeeBillService.createUtilityBill`（单号 UBILL-E/W）；`CheckinService.listActiveRecordsByRoom`（按房取在住）；账单列表加 `billType` 筛选。退宿挂账 `settleArrearsForRecord` 天然把住宿费+水电未缴一起挂账（无需改动）。
- **接口**（均 JWT）：`GET/PUT /api/fee/utility-rate`、`GET/POST /api/fee/meter-readings`、`POST /api/fee/utility-bills/generate`。
- 金额全程 `BigDecimal`（DECIMAL 列）。

### 前端
- 「费用管理」菜单组加「**抄表/水电**」页：单价配置条 + 楼栋→房间联动录入抄表（显示上期/用量/金额）+ 台账表格 + 「生成水电账单」按钮。
- 账单页加 **类型列 + 类型筛选**（住宿费/电费/水费）+ 明细列（remark）；挂账账单仍可缴费。
- `api/fee.ts` 加 utility-rate/meter-readings/utility-bills 函数；types 加 `MeterReading`/`UtilityRate`、`FeeBill` 加 `billType`/`remark`；dict 加 `BILL_TYPE`/`METER_TYPE`。

### 数据库
- 种子：单价（电 1.00 / 水 5.00）+ A102 房 2026-06 电表（用量 30→30.00）、水表（用量 8→40.00）抄表，便于开箱生成演示。

## 测试与验证
- 后端**全量回归 100 个测试全过**（含本期新增：FeeBillType 类型隔离、MeterService 抄表自动算量/幂等、UtilityBillGenerate 均摊+余数+无在住跳过+幂等、MeterController 接口鉴权、CheckinActiveByRoom）。
- 前端 `vue-tsc --noEmit` 零错误。
- 端到端联调（live curl，admin/admin123）：单价 1.00/5.00；台账 A102 电30/水40；生成 2026-06 → generated:2；电费账单回显 `UBILL-E-1-202606` billType=2 remark「电费 30.00度×1.00 ÷1人」；录入 2026-07 读数；幂等重生 generated:0/skipped:2。中文编码正常。
- 代码审查：本轮为内联自评（事务/均摊余数/幂等隔离/金额/复用/抄表上期逻辑均核对通过）；唯一 Minor（DEMO 可接受）——账单生成以"房间首位在住人是否已出账"做读数级幂等，若同期首次生成后房间在住人变动，重跑不会给新增人补账。

> 注：本子项目实现期间多智能体子 Agent 派发持续遇 API 529 过载，故改在主循环内联逐 Task 实现（仍 TDD 红→绿 + 每 Task 提交）；最终整体审查亦为内联自评，未跑独立多智能体审查。

## 不在本次范围（后续）
- 阶梯水电价、按期可变单价、整栋总表分摊、公摊/损耗。
- 滞纳金、按天比例、抄表 Excel 导入、用量趋势图。
- DB 级唯一约束/外键（切 MySQL 时统一补）。
