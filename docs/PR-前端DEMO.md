# PR 描述 — 宿舍管理 DEMO 前端 + 数据展示增强（ChipMore Dorm）

> 用于 `feat/demo-frontend` → `main` 的合并请求。

## 标题

```
feat: 宿舍管理 DEMO 前端（Vue3 + Element Plus）+ 看板数据增强 + ChipMore Dorm 品牌
```

## 概述

在已合并的后端 DEMO 基础上，实现完整的 **Web 管理后台前端**，并对房间状态看板等做了一轮面向宿管的**数据展示增强**。整体采用 **Apple 风**视觉，品牌定名 **ChipMore Dorm**。前后端已完成浏览器端到端联调（登录 → 资源管理 → 看板），控制台零报错。

## 技术栈

Vue 3 + Vite 5 + TypeScript + Element Plus + Pinia + Vue Router + Axios

## 主要内容

### 1. 前端基础设施
- Vite + Vue3 + TS 工程脚手架，`/api` 代理到后端 `:8080`
- axios 封装：自动注入 JWT、统一拆 `R{code,message,data}` 信封、错误提示、401 自动跳登录
- Pinia 用户 store（token 持久化）、Vue Router + 登录守卫、按模块封装的 API 层与类型、枚举字典

### 2. 页面（登录 + 后台外壳 + 5 个业务页）
- 登录页、后台外壳（侧边栏 + 顶栏）
- 楼栋管理（分页/筛选/增改删）
- 楼层管理（按楼栋）
- 房间管理（楼栋-楼层联动筛选）
- 床位管理（楼栋→楼层→房间级联）
- 房间状态看板

### 3. Apple 风全局主题
- `src/styles/theme.css`：设计令牌 + Element Plus 变量覆盖（毛玻璃卡片、大圆角、单一蓝强调、系统字体）
- 浅色毛玻璃侧栏、渐变毛玻璃登录卡
- （另含 4 套风格小样备选：`docs/style-previews/`）

### 4. 看板数据增强 v2（面向宿管）
- 入住率总览条（入住率 / 空闲床位 / 已满 / 维修中 / 房间 / 床位 / 地址）
- 图例可点击筛选（带各状态计数）
- **按楼层分组**展示，每层带小计
- 房间格性别标识（♂/♀）、床位占用小图标
- 房间管理：行内展开床位明细、配套设施 JSON 解析为标签、"标记维修/恢复空闲"快捷切换、接收楼层页跳转参数
- 楼层管理：房间数/床位数改为**实时统计** + 入住进度条 + "查看房间"跳转

### 5. 品牌
- 定名 **ChipMore Dorm**，重设计登录页（左右分栏 + 自制芯片电路意象 SVG 背景）

## 配套后端小改（同分支内）
- `RoomBoardVO` 增加 `floorNumber` / `genderLimit`，看板接口填充楼层号
- 新增 `FloorVO`，楼层列表接口返回**实时统计**的 roomCount / bedCount / occupiedBeds（替代静态种子值）
- 修复 Windows 下中文乱码：`application.yml` 加 `spring.sql.init.encoding: UTF-8` + pom 加 `project.build.sourceEncoding`
- 后端测试 **19 个全部通过**（含楼层统计断言）

## 如何运行

```
# 后端（:8080）
cd dms-backend && mvn spring-boot:run
# 前端（:5173，代理 /api）
cd dms-frontend && npm install && npm run dev
```

登录：`admin / admin123`

## 验证

前端 `vue-tsc` 类型检查零错误；浏览器端到端实测：登录、楼栋/楼层/房间/床位（含级联与行展开）、看板（分组/筛选/统计）全部正常，控制台无报错。效果截图见 `docs/style-previews/`（v2-login / v2-board / v2-rooms-expand 等）。

## 不在本次范围

入住/退宿/费用/维修/巡检/访客等业务模块、完整 RBAC、移动端、MySQL/Redis/Docker、第三方对接；看板悬停详情、入住人姓名、筛选汇总、楼栋卡片化等增强项留待后续。
