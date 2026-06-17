# PR 描述 — 账单报表 / 用量趋势

> 用于 `feat/report-stats` → `main` 的合并请求。基于最新 main 切出，**无合并顺序约束**（依赖的水电模块 PR #9 已合入 main）。

## 标题

```
feat: 账单报表/用量趋势（账期/楼栋汇总 + 欠费排行 + 水电用量趋势，ECharts 可视化）
```

## 概述

第二阶段第六个子项目「账单报表 / 用量趋势」。在已合并的费用 + 水电模块基础上，新增**只读统计报表**：对现有 `dms_fee_bill`（账单）+ `dms_meter_reading`（抄表台账）做内存聚合，提供四块——按账期汇总、按楼栋汇总、欠费排行、水电用量趋势，前端引入 ECharts 出图。**不新增表、不改现有数据，纯读。** 仍沿用 H2 内存库 DEMO，后端 TDD 逐 Task 实现（子 Agent 驱动 + 两阶段审查 + 最终整体审查）。

设计与计划：`docs/superpowers/specs/2026-06-17-账单报表用量趋势-design.md`、`docs/superpowers/plans/2026-06-17-账单报表用量趋势.md`。

## 主要内容

### 后端
- **无新表**。`module/fee` 下新增只读报表三件套 + 4 VO：`ReportService`/`ReportServiceImpl`/`ReportController`，`vo/{PeriodSummaryVO, BuildingSummaryVO, ArrearsRankVO, UsageTrendVO}`。
- **内存聚合**：直接用 `FeeBillMapper`/`MeterReadingMapper` `selectList` 取数后 Java stream / `TreeMap` 分组求和（DEMO 数据量小，不写自定义 SQL，便于单测）。名称回显复用 `RoomService`/`BuildingService`/`ResidentService`（room→building 做 memoization 避免重复查询）。
- **四个接口**（均 GET、JWT、挂 `/api/report`）：
  - `GET /api/report/period-summary` → 按账期：住宿/电/水拆分 + 应收/已缴/未缴 + 收缴率（period 升序）。
  - `GET /api/report/building-summary` → 按楼栋：应收/已缴/未缴 + 收缴率（buildingId 升序，楼栋名回显）。
  - `GET /api/report/arrears-ranking?limit=10` → 欠费排行：按居住人聚合欠费额/张数，降序取前 limit（默认 10）。
  - `GET /api/report/usage-trend` → 水电用量趋势：抄表台账按账期分电/水用量合计（period 升序）。
- **统一口径**：应收 = 非作废账单(status≠3) 金额合计；已缴 = status 2；未缴 = status 1(未缴)+4(挂账)；收缴率 = 已缴×100/应收（2 位 HALF_UP，应收为 0 → 0）；billType 2=电 / 3=水 / 其余=住宿。四块口径一致，收缴率走单一 `collectRate` 助手。
- 金额全程 `BigDecimal`。

### 前端
- 引入 `echarts` + `vue-echarts`；`main.ts` 按需注册组件（LineChart/BarChart + Grid/Tooltip/Legend/Title + CanvasRenderer）并全局注册 `<v-chart>`。
- 新增菜单组「**统计报表**」+ 页面 `views/report/index.vue`（四卡片）：账期汇总（住宿/电/水堆叠柱 + 收缴率折线双轴 + 明细表）、水电用量趋势（电/水折线）、按楼栋汇总（横向堆叠柱）、欠费排行（Top 10 表）。
- `api/report.ts` 加 4 个查询函数；`types.ts` 加 `PeriodSummary`/`BuildingSummary`/`ArrearsRank`/`UsageTrend` 四接口（字段与后端 VO 一一对应）。

### 数据库
- 无 schema/种子改动（纯读现有数据）。

## 测试与验证
- 后端**全量回归 111 个测试全过**（含本期新增：`ReportServiceTest` 账期拆分+收缴率+作废不计+未缴含挂账+total0→0+升序、楼栋 delta 断言+名称回显、欠费分组+降序+limit；`ReportControllerTest` 四接口 + 无 token→401）。测试用独立账期（2099-xx）插入已知数据并按账期/楼栋过滤断言，全局口径用增量(delta)断言避免种子干扰，`BigDecimal.compareTo` 断言忽略 scale。
- 前端 `vue-tsc --noEmit` 零错误。
- 代码审查：子 Agent 驱动逐 Task 实现，账期汇总任务过两阶段审查（spec 合规 + 代码质量，均通过），其余结构同构任务核对 diff 与计划一致；最终整体审查（独立 Agent，全功能 diff）结论 **Ready to merge**，确认四块口径一致、前后端类型对齐、5 个依赖均被使用、测试断言真实行为，无 Critical/Important 问题。

> 已知（非本次范围）：`npm run build`（`vue-tsc -b`）因**既有** `vite.config.ts` 引 `node:url` 而项目未装 `@types/node` 报错——该问题在 main 上同样存在、与本功能无关，本仓库历来以 `vue-tsc --noEmit` 作为前端类型门禁（已通过）。可另起小修补 PR 加 `@types/node` 修复整体构建。

## 不在本次范围（后续）
- 报表导出 CSV/Excel。
- 按房间/楼层下钻趋势、自定义时间范围筛选、同比/环比。
- 定时报表快照表、缓存。
