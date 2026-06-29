# 维修工单管理 PR 描述

## 变更概览
- 新增后端 epair 模块：维修工单建单、列表、详情、受理、完成、取消。
- 新增 dms_repair_order 表和 3 条演示种子数据。
- 新增前端「维修管理 / 维修工单」页面，支持筛选、新建、受理、完成、取消。
- 新增维修状态/紧急程度字典和前端 API/types。

## 验证
- cd dms-backend && mvn test：118 tests passed。
- cd dms-frontend && npm run build：通过。

## 非目标
- 不联动房间状态。
- 不生成维修费用账单。
- 不接 OA/webhook 或居住人自助端。