# PR 描述 — 入住管理子项目（居住人 + OA/HCP 对接 + 入住流程）

> 用于 `feat/checkin-module` → `main` 的合并请求。

## 标题

```
feat: 入住管理子项目（居住人档案 + OA/HCP webhook 对接 + 两段式入住）
```

## 概述

第二阶段第一个业务子项目「入住管理」。在已合并的宿舍资源管理 DEMO 基础上，新增**居住人/员工档案**、**OA/HCP 外部系统对接（webhook 防腐层 + 预留接口）**、以及**两段式入住流程**（意向单 → 管理员选床确认入住 → 入住档案）。仍沿用 H2 内存库 DEMO 形态，零外部依赖启动。

设计与计划：`docs/superpowers/specs/2026-06-15-入住管理子项目-design.md`、`docs/superpowers/plans/2026-06-15-入住管理子项目.md`。

## 主要内容

### 后端（新增三模块 + 复用 resource）
- **resident**：居住人/员工档案（工号/姓名/性别/类型/部门/手机/证件），CRUD + 按工号幂等 upsert。
- **checkin**：
  - 入住意向单 `dms_checkin_intake`（待分配/已入住/已取消，来源 OA/HCP/手工，幂等键 biz_no）
  - 入住档案 `dms_checkin_record`（人-楼栋/楼层/房间/床位-入住时间-状态）
  - 选床确认入住为**单事务**：性别校验（vs 房间性别限制）→ 占用床位 → 刷新房间 occupied_beds/status → 生成档案 → 意向单置已入住
- **integration（防腐层）**：
  - `POST /api/integration/oa/checkin-application`、`POST /api/integration/hcp/employee`
  - 鉴权：`X-Integration-Token` 头（`application.yml` 的 `integration.token`，DEMO 值 `dms-demo-integration-token`），独立于 JWT
  - 适配器把外部报文映射成内部命令；按 biz_no（OA 申请单号 / HCP `HCP-{工号}-{入职日}`）幂等去重
  - 出站 `OaClient`/`HcpClient` 为预留空实现（log 占位），真实 OA/HCP 细节到位仅替换适配器/客户端
- 复用 `resource` 模块：新增 `BedService.occupy/getById`、`RoomService.refreshOccupancy`（按模块边界，checkin 不直接碰 bed/room mapper）

### 前端（新增菜单组「入住管理」）
- 居住人管理（搜索 + 表格 + 增改删）
- 入住意向单（状态/来源筛选 + 手工新建 + **分配入住弹窗**：楼栋→房间→空闲床位联动选择 + 取消）
- 入住档案（只读列表，按楼栋筛选）

### 数据库
- 新增 3 表 + 种子（3 居住人、1 已入住档案、1 待分配意向单），便于开箱演示。

## 测试与验证
- 后端 **41 个测试全过**（resident CRUD/工号查重/幂等 upsert、占床+房间统计刷新、assign 事务+性别校验+床位占用拒绝、OA/HCP webhook 幂等+token 校验、controller 流程）。
- 前端 `vue-tsc --noEmit` 零错误。
- 端到端联调：向两个 webhook 投样例报文 → 意向单列表出现"待分配"（OA 赵六 / HCP 新人甲）→ 浏览器分配入住 → 入住档案"在住"、床位占用。截图见 `docs/style-previews/checkin-{intakes,records}.jpeg`。

## 配套约定 / 预留
- OA/HCP 真实对接细节（报文格式、地址、真实鉴权、出站回写）尚未提供，本期以防腐层 + 预留接口承接；将来只换适配器实现，不动入住核心逻辑。

## 不在本次范围（后续子项目）
- 退宿（释放床位；`BedService.release` 已占位，需补 `current_user_id` 置 null 的 MyBatis-Plus 字段策略）
- 费用（水电费/账单/结算）
- 多节点审批流、员工自助端、智能分配、集成事件表/重试/审计、MySQL/Docker 部署
