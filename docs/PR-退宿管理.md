# PR 描述 — 退宿管理子项目（退宿单 + OA 退宿/离职对接 + 办理退宿）

> 用于 `feat/checkout-module` → `main` 的合并请求。

## 标题

```
feat: 退宿管理子项目（退宿单 + OA 退宿申请/离职 webhook + 事务化办理退宿）
```

## 概述

第二阶段第二个业务子项目「退宿管理」。在已合并的入住管理（PR #5）基础上，新增**退宿单**、**OA 退宿申请单 / 离职单两个 webhook 对接（防腐层 + 匹配级联 + 幂等）**、以及**事务化办理退宿流程**（释放床位 + 刷新房间统计 + 归档在住档案）。同时修复入住遗留的床位释放 Bug。仍沿用 H2 内存库 DEMO 形态，零外部依赖启动。

设计与计划：`docs/superpowers/specs/2026-06-15-退宿管理子项目-design.md`、`docs/superpowers/plans/2026-06-15-退宿管理子项目.md`。

## 主要内容

### 后端（新增 checkout 模块 + 扩 integration/checkin/resident/resource）
- **checkout（新模块）**：
  - 退宿单 `dms_checkout_order`（待退宿/已退宿/已取消，来源 退宿申请/离职/手工，幂等键 biz_no）
  - 列表（分页 + 状态/来源筛选 + 居住人姓名/工号 + 关联房间/床位回显）
  - 手工新建（校验居住人有在住档案，无则拒绝）
  - 命令建单 `createOrderFromCommand`：供适配器调用，按 biz_no 幂等
  - **办理退宿 `confirm` 为单 `@Transactional`**：释放床位 → 刷新房间 occupied_beds/status → 在住档案置已退宿 + 回填 checkout_date → 退宿单置已退宿
- **integration（扩 oa 防腐层）**：
  - `POST /api/integration/oa/checkout-application`（退宿申请单）、`POST /api/integration/oa/resignation`（离职单）
  - 鉴权：复用 `X-Integration-Token` 头（DEMO 值 `dms-demo-integration-token`），独立于 JWT
  - **匹配级联四种 outcome**：`NO_RESIDENT`（无居住记录）/ `NO_ACTIVE_CHECKIN`（退宿申请但无在住，拒绝）/ `RESIGNED_NO_CHECKIN`（离职但无在住 → 置居住人离职、不建单）/ `ORDER_CREATED`（在住 → 建待退宿单；离职额外置居住人离职）
  - 出站 `OaClient.notifyCheckoutCompleted` 为预留空实现（log 占位，与入住侧出站客户端一致）
- **checkin（扩服务）**：`CheckinRecord` 加 `checkoutDate`；新增 `getRecord` / `findActiveRecordByResident` / `markCheckedOut`
- **resident（扩服务）**：新增 `markResigned`（离职置 status=0）
- **resource（修复）**：`Bed.currentUserId` 加 `@TableField(updateStrategy = FieldStrategy.ALWAYS)`，修复 `release` 置 null 不持久化的入住遗留 Bug（MyBatis-Plus 默认跳过 null 字段）

### 前端（「入住管理」菜单组加「退宿单」页）
- 退宿单列表（状态/来源筛选 + 分页）
- 手工新建弹窗（从在住档案下拉选居住人 + 预计退宿日 + 原因）
- 办理退宿弹窗（显示居住人/房间/床位 + 选退宿日期 → 确认）
- 取消退宿单

### 数据库
- 新增 1 表 `dms_checkout_order` + `dms_checkin_record` 加 `checkout_date` 列 + 一条待退宿单种子（SEED-CO-1），便于开箱演示。

## 测试与验证
- 后端**全量回归 62 个测试全过**（含本期新增：Bed release 置 null 持久化、CheckinRecord 读写、Resident markResigned、checkout 建单/幂等/取消、confirm 事务、Controller 鉴权流程、OA webhook 匹配级联 + token）。
- 前端 `vue-tsc --noEmit` 零错误。
- 端到端联调（live curl，admin/admin123）：
  - 4 种 webhook outcome 全验证（E1001 在住→ORDER_CREATED、E9999→NO_RESIDENT、E2001 离职无在住→RESIGNED_NO_CHECKIN+置离职）
  - 重复推送同一 biz_no 幂等（返回同一 orderId）
  - 错误 token → code 40100
  - 登录 → 列退宿单（姓名/工号/房间/床位回显）→ 办理退宿 → 单变已退宿、**bed3 释放（current_user_id=null、status=空闲）**
  - 中文编码全程正常

## 配套约定 / 预留
- OA 退宿/离职真实对接细节（报文格式、地址、真实鉴权、退宿完成回写）尚未提供，本期以防腐层 + 预留接口承接；将来只换适配器/客户端实现，不动退宿核心逻辑。

## 不在本次范围（依赖费用子项目）
- 费用结算 / 退宿检查（水电欠费拦截）/ 损坏赔偿挂账
- 多节点退宿审批流、员工自助端、集成事件表/重试/审计、MySQL/Docker 部署

## 后续子项目
- **费用**（水电费 / 账单 / 结算；退宿的费用结算挂账依赖它）
