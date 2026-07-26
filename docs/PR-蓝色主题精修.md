# PR 描述 — 蓝色企业主题 2.0：品牌精修 + 紧凑密度

> 用于 `codex/operations-ux-fixes` → `main` 的合并请求。

## 标题

```
feat: 蓝色主题 2.0 精修（靛蓝品牌色 + 紧凑密度 + 外壳细节）
```

## 概述

在既有蓝色企业主题基础上做精修，**仅改动全局样式令牌与外壳骨架**，业务页面经 `--dms-*` 变量自动继承，零业务逻辑改动。解决两个问题：① 原主题「模板感」重、品牌辨识度弱；② 内容密度低，页面大量区域需要滚动。

## 主要内容

### 1. 主题令牌（`src/styles/theme.css`）

- 品牌色：`#1f6feb` → 靛蓝 `#2b5ce6`（strong `#1e46b8`），`--el-color-primary-light-*` 阶梯同步重算；`--dms-warn` 略降饱和
- 阴影三档化：`--dms-shadow-card`（静止）/ 新增 `--dms-shadow-raised`（hover 浮起）/ `--dms-shadow-float`（弹窗）；卡片加过渡
- 内容区底色叠加极淡顶部径向微光，背景不再死平
- 紧凑密度：`--el-component-size: 30px`、卡片体 padding 20→16/18、表格行高 44→38、表头 12px/600、圆角 14/10→12/8
- 组件细节：弹窗头部加底线、底部浅灰带分层；主按钮阴影减弱 + `:active` 按压感；表单 label 统一灰调 500 字重；focus 描边色同步新品牌色

### 2. 外壳（`src/layout/index.vue`）

- 侧栏 232→204px；选中项从「整块高饱和蓝」改为「左侧 3px 发光指示条 + 半透明白底」；logo 方块改品牌渐变；侧栏右缘加分层线
- 顶栏 60→50px：左侧静态文字改为**当前页面名**（`route.meta.title`）；用户头像改品牌渐变圆形；用户区加 hover 浅底；顶栏底部加极淡投影
- 内容区 padding 24→16

### 3. 页面骨架（`src/components/layout/AppPage.vue`）

- 标题区 64→40px、标题 28→22px、页面间距 20→14

### 4. 色值对齐

- 登录页电路网格、报表图表色板（`report/index.vue`）中的旧蓝色值同步为新靛蓝；登录页整体 Apple 风保留

## 验证

- `npm run build`（vue-tsc + vite）通过
- 本地前后端真机截图留档：`docs/style-previews/refine-live-{login,dashboard,board,buildings}.jpeg`
- 设计预览页：`docs/style-previews/blue-refine-preview.html`（右下角可切换 标准/紧凑 密度对比，`#screen1/#screen2` 分段查看）

## 不在本次范围

- 业务页面 template/script、路由、接口零改动
- 不做暗色模式/多主题切换
