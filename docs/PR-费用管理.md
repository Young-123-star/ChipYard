# PR 描述 — 费用管理子项目（住宿费账单闭环）

> 用于 `feat/fee-module` → `main` 的合并请求。

## 标题

```
feat: 费用管理子项目（收费标准 + 按月生成住宿费账单 + 缴费/作废）
```

## 概述

第二阶段第三个业务子项目「费用管理」。在已合并的入住（PR #5）/退宿（PR #6）基础上，新增**住宿费账单闭环**：收费标准（房型→月单价）维护 → 按月幂等生成账单（金额快照）→ 缴费登记 → 作废 / 欠费查询。仍沿用 H2 内存库 DEMO 形态，零外部依赖启动；子 Agent 驱动 + TDD 实现。

设计与计划：`docs/superpowers/specs/2026-06-16-费用管理子项目-design.md`、`docs/superpowers/plans/2026-06-16-费用管理子项目.md`。

## 主要内容

### 后端（新增 fee 模块 + 扩 checkin）
- **fee（新模块）**：
  - 收费标准 `dms_fee_standard`（room_type → 月单价，房型唯一）：`FeeStandardService` CRUD（create/update 均查重）。
  - 账单 `dms_fee_bill`（关联在住档案 + 金额快照 + 状态 未缴/已缴/已作废）：`FeeBillService`
    - **生成账单**（`@Transactional`，幂等批量）：对所有在住档案各生成一张该账期账单，金额=房型月单价快照；`(档案,账期)` 已存在则跳过；无收费标准则跳过；返回 `{generated, skipped}`。
    - **缴费**：未缴 → 已缴 + 记缴费时间/方式（现金/转账）；非未缴拒绝。
    - **作废**：未缴 → 作废；非未缴拒绝。
    - **欠费查询** = 账单列表按「未缴」筛选。
- **checkin（扩服务）**：新增 `listActiveRecords()`（列出在住档案，供账单生成遍历，保持模块边界）。
- 金额全程用 `BigDecimal`（schema `DECIMAL(10,2)`）；缴费走 `PayBillDTO` 校验，生成账期走 `@Pattern(yyyy-MM)` 校验。

### 前端（新增菜单组「费用管理」）
- 收费标准页（表格 + 新增/编辑/删除，房型用现有字典）。
- 住宿费账单页：账期/状态筛选 + 汇总条（共 N 张 + 本页已缴/未缴/金额合计）+「生成账单」（选账期，toast「生成 N 张 / 跳过 M 张」）+ 缴费（现金/转账）+ 作废。
- 新增 `api/fee.ts`、types 加 FeeStandard/FeeBill、dict 加 BILL_STATUS/PAY_METHOD。

### 数据库
- 新增 2 表 + 种子（3 条收费标准覆盖房型 1/2/3、张三 record1 的 2026-06 账单），便于开箱演示。

## 测试与验证
- 后端**全量回归 75 个测试全过**（含本期新增：CheckinListActive、FeeStandardService CRUD+查重、FeeBillService 生成幂等+金额快照+跳过/缴费/作废状态守卫、FeeController 鉴权）。
- 前端 `vue-tsc --noEmit` 零错误。
- 端到端联调（live curl，admin/admin123）：
  - 收费标准列表（3 条，中文房型/价格正确）
  - 生成 2026-07 → `{generated:1, skipped:0}`；重复生成 → `{generated:0, skipped:1}`（幂等）
  - 账单列表回显居住人/工号/房间/房型（张三·E1001·A102·双人间·800.00）
  - 缴费 → 已缴 + paidAt + payMethod；重复缴费拒绝（业务码 50000）
  - 无 token → 401
  - 中文编码全程正常
- 代码审查（最终整体审查）：无 Critical/Important；按审查建议补充了缴费 DTO 校验、账期格式校验、账单页「本页合计」标注。

## 不在本次范围（后续子项目）
- 水电费抄表计费、押金、滞纳金/催缴、部分缴费 / 缴费流水表。
- 退宿欠费拦截 / 结算挂账（退宿模块本轮不动）。
- 账单 PDF/导出、缴费对接支付网关、按天比例计费。
- DB 级唯一约束/外键（切 MySQL 时统一补）。

## 后续可做方向
- 水电费抄表计费 / 押金 / **退宿欠费结算挂账**（把费用与已完成的退宿模块打通）。
