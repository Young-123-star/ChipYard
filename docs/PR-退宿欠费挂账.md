# PR 描述 — 退宿欠费结算挂账（费用 × 退宿打通）

> 用于 `feat/checkout-arrears` → `main` 的合并请求。

## 标题

```
feat: 退宿欠费结算挂账（办理退宿自动挂账未缴账单 + 离场欠费记录）
```

## 概述

第二阶段第四个子项目，把已合并的**费用模块**（PR #7）与**退宿模块**（PR #6）打通。办理退宿时在**同一事务**内自动「欠费结算挂账」：该在住档案的未缴账单置「挂账」状态、退宿单回填「离场欠费金额」，退宿流程**不阻断**（挂账放行）。挂账账单后续仍可缴费结清。仍沿用 H2 内存库 DEMO，子 Agent 驱动 + TDD。

设计与计划：`docs/superpowers/specs/2026-06-16-退宿欠费结算挂账-design.md`、`docs/superpowers/plans/2026-06-16-退宿欠费结算挂账.md`。

## 主要内容

### 后端
- **数据改动仅 2 处，不新增表**：`dms_checkout_order` 加 `arrears_amount DECIMAL(10,2) DEFAULT 0`；账单状态新增取值 **4 = 挂账**（复用 `dms_fee_bill.status`）。
- **fee 模块**：
  - `FeeBillService.listUnpaidByRecord(recordId)`：查某在住档案未缴账单。
  - `FeeBillService.settleArrearsForRecord(recordId)`：未缴账单置挂账(4)，返回欠费总额（无则 0）。
  - `pay` 守卫放宽为「未缴(1) 或 挂账(4) 可缴费」（挂账后续可结清→已缴）；`void` 维持仅未缴可作废。
  - 新增只读 `GET /api/fee/arrears?checkinRecordId=` → `{count, totalAmount}`（退宿弹窗欠费预览）。
- **checkout 模块**：`CheckoutOrder`/`CheckoutOrderVO` 加 `arrearsAmount`；`CheckoutServiceImpl.confirm`（已 `@Transactional`）末尾注入 `FeeBillService` 调 `settleArrearsForRecord` 并回填欠费总额。模块依赖方向 **checkout → fee**（无环）。
- 金额全程 `BigDecimal`；办理退宿仅对待退宿(1)单允许，幂等无重复挂账。

### 前端
- **退宿单页**：「办理退宿」弹窗打开时拉取并显示待结算欠费（warning alert：N 张 / ¥X；无欠费显示 success；加载失败显示兜底提示）；列表加「离场欠费」列（>0 标红）。
- **账单页**：`BILL_STATUS` 加「挂账」（可筛选）；挂账账单操作列保留「缴费」按钮（作废仅未缴）。

### 数据库
- `arrears_amount` 默认 0，旧种子行（SEED-CO-1）自动为 0；办理种子退宿单即可演示挂账（张三 2026-06 账单 800.00）。

## 测试与验证
- 后端**全量回归 83 个测试全过**（含本期新增：FeeArrearsService 的 listUnpaid/settle/pay 放宽、FeeArrearsController 的 arrears 接口+鉴权、CheckoutArrears 的 confirm 集成）。
- 前端 `vue-tsc --noEmit` 零错误。
- 端到端联调（live curl，admin/admin123）：
  - 欠费预览 `count=1, total=800.00`
  - 办理退宿 → 退宿单 `arrearsAmount=800.00`、`status=2`（已退宿）
  - 账单 `status=4`（挂账）
  - 挂账账单缴费 → 已缴
  - 再查预览 → `count=0, total=0`
- 代码审查（最终整体审查）：事务完整性/金额/状态模型/范围/依赖方向/幂等均通过；按审查修复了「挂账账单前端缺缴费按钮」（Important）与「退宿弹窗欠费加载失败误显示无欠费」（Minor）。

## 不在本次范围（后续子项目）
- 滞纳金、部分缴费、坏账核销、催缴通知、挂账账龄/账期归集报表。
- 退宿硬拦截（本设计为挂账放行，不阻断）。
- 按天比例计费、水电抄表、押金、DB 级唯一约束/外键。
