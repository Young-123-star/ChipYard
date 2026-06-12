# DMS 前端（DEMO）

宿舍管理系统 Web 管理后台。Vue3 + Vite + Element Plus + Pinia + Vue Router。

## 运行（需后端先在 :8080 启动）

```
npm install
npm run dev        # http://localhost:5173，/api 代理到 :8080
```

后端启动：`cd ../dms-backend && mvn spring-boot:run`

## 登录

admin / admin123

## 功能

- 登录（JWT，路由守卫，401 自动跳登录）
- 楼栋管理（分页/筛选/增改删）
- 楼层管理（按楼栋）
- 房间管理（楼栋-楼层联动筛选，增改删）
- 床位管理（楼栋→楼层→房间级联）
- 房间状态看板（按状态着色网格）

## 构建

```
npm run build      # 类型检查 + 产物 dist/
```

## 结构

```
src/
├── api/        # 按模块封装请求（auth/building/floor/room/bed）+ types
├── utils/      # request.ts（axios 拦截器）/ dict.ts（枚举字典）
├── stores/     # Pinia: user（token/用户信息）
├── router/     # 路由 + 登录守卫
├── layout/     # 后台外壳（侧边栏+顶栏）
└── views/      # login / resource/{building,floor,room,bed,board}
```
