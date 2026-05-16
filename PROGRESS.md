# LifeTrack 项目进度跟踪

> 最后更新: 2026-05-16 (Phase 5 + 体重编辑 + 趋势范围 + 仪表盘完成)

## 当前阶段

**Phase 5: 仪表盘 + 数据可视化 — 已完成**

---

## 已完成清单

### Phase 1: 项目基础设施 + 用户认证

<details>
<summary>点击展开 Phase 1 详情</summary>

### 后端 (lifetrack-server/ — Spring Boot 3.3.6 + Java 21)
- [x] Maven 项目骨架, pom.xml 依赖管理
- [x] Flyway 数据库迁移 V1: 11 张表 + 默认数据 (分类/食物)
- [x] MyBatis-Plus 3.5.9 配置 (逻辑删除、自动填充、驼峰映射)
- [x] 7 个 Entity: User, Task, TaskCheckin, WeightRecord, DietRecord, Food, LedgerRecord
- [x] 7 个 Mapper (BaseMapper)
- [x] 统一响应 Result<T> + 全局异常处理 GlobalExceptionHandler
- [x] JWT 认证: JwtUtils 生成/验证, JwtAuthenticationFilter
- [x] Spring Security: BCrypt 密码加密, /api/auth/** 放行, 其余需认证
- [x] Swagger/OpenAPI: springdoc-openapi, http://localhost:8088/docs
- [x] AuthController: register / login / getProfile / updateProfile
- [x] AuthService: 注册 (邮箱唯一校验), 登录 (JWT签发), 个人信息 CRUD
- [x] DTO/VO: RegisterDTO, LoginDTO, UpdateProfileDTO, LoginVO, UserProfileVO

### 前端 (lifetrack-web/ — Vue 3 + Vite + Element Plus + Pinia)
- [x] Vite 项目脚手架, npm 依赖安装
- [x] Element Plus 全局注册
- [x] Vue Router: /auth/login, /auth/register, /dashboard, 路由守卫 (token校验)
- [x] Pinia authStore: token 持久化 (localStorage), login/register/logout/fetchProfile
- [x] Axios 封装: JWT 拦截器, 统一错误提示, 401 自动跳转登录页
- [x] Login.vue: 邮箱+密码表单, Element Plus 校验, 登录后跳转 dashboard
- [x] Register.vue: 邮箱+密码+确认密码, 注册成功跳转登录页
- [x] Dashboard.vue: 基础布局 (四卡片占位: 任务/体重/热量/收支), 退出登录
- [x] Vite proxy: /api → http://localhost:8088

### 数据库
- [x] MySQL 8.0.42, 数据库 lifetrack, 字符集 utf8mb4
- [x] Flyway 自动建表 (首次启动时)
- [x] 默认分类数据 (11条 ledger_categories)
- [x] 默认食物数据 (14条 foods)

### API 验证
- [x] POST /api/auth/register → 200
- [x] POST /api/auth/login → 200 (返回 JWT Token)
- [x] GET /api/user/profile → 200 (需 Bearer Token)
- [x] Swagger UI 可访问

</details>

### Phase 3: 健康记录模块

<details>
<summary>点击展开 Phase 3 详情</summary>

### 后端新增
- [x] DTO: CreateWeightRecordDTO, CreateDietRecordDTO
- [x] VO: WeightRecordVO, WeightTrendVO, DietRecordVO, DietStatsVO
- [x] HealthService: 体重 CRUD + 趋势(90天) + 饮食 CRUD + 统计(按日期汇总) + 食物搜索
- [x] HealthController: 8 个 REST 端点
- [x] CreateDietRecordDTO.recordTime 添加 @JsonFormat 支持空格分隔日期

### 前端新增
- [x] api/health.js: 8 个 API 函数封装
- [x] views/health/HealthPage.vue: 双 Tab 页面 (体重趋势折线图 + 饮食记录柱状图 + 记录列表 + 新增对话框)
- [x] ECharts 图表: 体重趋势绿色渐变面积图 + 近7天热量琥珀色柱状图
- [x] 食物搜索: el-select remote 远程搜索，选择后自动填充热量/营养素

### 前端修改
- [x] router/index.js: 添加 /health 路由
- [x] Dashboard.vue: 侧边栏"健康记录"启用为 router-link
- [x] DashboardHome.vue: "体重记录"和"热量摄入"卡片启用跳转 /health

### API 验证 (全部通过)
- [x] POST /api/health/weights → 200 (添加体重记录)
- [x] GET /api/health/weights?page=1&size=10 → 200 (分页列表)
- [x] DELETE /api/health/weights/{id} → 200 (删除记录)
- [x] GET /api/health/weights/trend?days=90 → 200 (90天趋势，含 direction)
- [x] GET /api/foods?name=鸡 → 200 (模糊搜索)
- [x] POST /api/health/diets → 200 (添加饮食，含 food join 信息)
- [x] GET /api/health/diets?date=2026-05-16 → 200 (按日期筛选)
- [x] GET /api/health/diets/stats?date=2026-05-16 → 200 (每日热量/营养素汇总)

</details>

### Phase 3.5: 体重记录时段增强

<details>
<summary>点击展开 Phase 3.5 详情</summary>

### 数据库变更
- [x] Flyway V2 迁移: `weight_records` 表添加 `time_slot VARCHAR(10)` 字段 (MORNING/AFTERNOON/EVENING)

### 后端修改
- [x] WeightRecord 实体添加 `timeSlot` 字段
- [x] CreateWeightRecordDTO 添加 `timeSlot` 可选字段
- [x] WeightRecordVO 添加 `timeSlot` 字段
- [x] HealthService.addWeight(): 存储 timeSlot
- [x] HealthService.getWeightTrend(): 趋势查询按 timeSlot 排序 (早→中→晚, putIfAbsent 取早晨体重)
- [x] HealthService.toWeightVO(): 映射 timeSlot

### 前端修改
- [x] HealthPage.vue: 体重表格添加"时段"列 (el-table-column)
- [x] HealthPage.vue: 新增体重对话框添加时段选择器 (el-select: 早/中/晚)
- [x] HealthPage.vue: timeSlotLabel() 映射函数 (MORNING→早, AFTERNOON→中, EVENING→晚)

</details>

### Phase 2: 任务打卡模块

### 后端新增
- [x] DTO: CreateTaskDTO, UpdateTaskDTO, CheckinDTO
- [x] VO: TaskVO, TaskStatsVO, CheckinRecordVO
- [x] MyBatisPlusConfig: 添加分页插件 `PaginationInnerInterceptor`
- [x] pom.xml: 添加 `mybatis-plus-jsqlparser` 依赖
- [x] TaskService: 任务 CRUD + 打卡(3天补签) + 连续天数计算 + 统计
- [x] TaskController: 8 个 REST 端点

### 前端新增
- [x] vite.config.js: 添加 `@` 路径别名
- [x] api/tasks.js: 8 个 API 函数封装
- [x] views/tasks/TaskList.vue: 任务列表页 (统计卡片 + 筛选 + 任务卡片网格 + 新建对话框)
- [x] views/tasks/TaskDetail.vue: 任务详情页 (连续天数展示 + 84天日历热力图 + 打卡记录时间线 + 编辑/删除/打卡)
- [x] views/dashboard/DashboardHome.vue: 仪表盘首页 (4 模块导航卡片)
- [x] Dashboard.vue 重构为侧边栏布局 + 嵌套路由
- [x] router/index.js 路由重构为嵌套结构

### API 验证 (全部通过)
- [x] POST /api/tasks → 200 (创建任务)
- [x] GET /api/tasks?page=1&size=10 → 200 (任务列表+分页)
- [x] GET /api/tasks/{id} → 200 (任务详情)
- [x] PUT /api/tasks/{id} → 200 (更新任务)
- [x] DELETE /api/tasks/{id} → 200 (软删除)
- [x] POST /api/tasks/{id}/checkin → 200 (打卡, 连续天数+1)
- [x] GET /api/tasks/{id}/records → 200 (打卡历史)
- [x] GET /api/tasks/stats → 200 (打卡统计)

### Phase 4: 账本管理模块

<details>
<summary>点击展开 Phase 4 详情</summary>

### 后端新增
- [x] Entity: LedgerCategory (11 个系统默认分类), LedgerBudget
- [x] Mapper: LedgerCategoryMapper, LedgerBudgetMapper
- [x] DTO: CreateLedgerRecordDTO (type/amount/categoryId @NotNull), UpdateLedgerRecordDTO
- [x] VO: LedgerRecordVO (含 categoryName/categoryIcon join), LedgerStatsVO (含 CategoryItem 嵌套类)
- [x] LedgerService: 收支 CRUD + 分类列表 + 月度统计 (按分类汇总+占比) + CSV 导出 (BOM + 逗号转义)
- [x] LedgerController: 7 个 REST 端点

### 前端新增
- [x] api/ledger.js: 7 个 API 函数封装
- [x] views/ledger/LedgerPage.vue: 完整账本页面 (月度统计卡片 + 分类饼图 + 记录列表 + 筛选 + 新增/编辑对话框 + CSV 导出)
- [x] ECharts 饼图: 支出分类占比 (暖金调色板) + 收入分类占比 (蓝调色板)
- [x] 月度导航: 左右箭头切换月份

### 前端修改
- [x] router/index.js: 添加 /ledger 路由
- [x] Dashboard.vue: 侧边栏"账本管理"启用为 router-link，移除 disabled + 待上线标签
- [x] DashboardHome.vue: "本月收支"卡片启用，跳转 /ledger

### API 验证 (全部通过)
- [x] POST /api/ledger/records → 200 (添加支出/收入, 分类名正确join)
- [x] GET /api/ledger/records?page=1&size=10 → 200 (分页列表+分类名)
- [x] PUT /api/ledger/records/{id} → 200 (更新金额/备注)
- [x] DELETE /api/ledger/records/{id} → 200 (软删除)
- [x] GET /api/ledger/categories → 200 (11 个分类, 系统默认+用户)
- [x] GET /api/ledger/stats?year=2026&month=5 → 200 (totalIncome/totalExpense/balance + 分类占比)
- [x] GET /api/ledger/stats/export?year=2026&month=5 → 200 (CSV 含中文 BOM)

</details>

---

## 阶段完成进度

| Phase | 内容 | 状态 |
|-------|------|------|
| Phase 1 | 项目基础设施 + 用户认证 | 已完成 |
| Phase 2 | 任务打卡模块 | 已完成 |
| Phase 3 | 健康记录模块 | 已完成 |
| Phase 3.5 | 体重记录时段增强 | 已完成 |
| Phase 4 | 账本管理模块 | 已完成 |
| Phase 5 | 仪表盘 + 数据可视化 + 补充功能 | 已完成 |
| Phase 6 | 部署与测试 | 待开始 |

---

## Phase 2 问题记录

详见 [ISSUES.md](./ISSUES.md)（10 项：编译、依赖、图标、路由、编码等）

---

## 关键技术决策 (已确认)

- Java 21 (LTS) — 环境实际版本
- Spring Boot 3.3.6
- npm (非 pnpm)
- MySQL root 密码: 123456
- 后端端口: 8088
- 前端端口: 3000 (Vite 默认)
- Redis 后续再加
- 构建: Maven (后端) / npm (前端)
- MyBatis-Plus 3.5.9 分页需单独引入 `mybatis-plus-jsqlparser`

---

## 下次继续前需知

### 服务启动方式
```bash
# 后端 (端口 8088)
cd E:\workspaceForCC\LifeTrack\lifetrack-server
mvn spring-boot:run

# 前端 (端口 3000)
cd E:\workspaceForCC\LifeTrack\lifetrack-web
npm run dev
```

### 数据库连接
- Host: localhost:3306
- Database: lifetrack
- User: root
- Password: 123456

### Phase 5: 仪表盘 + 数据可视化 + 补充功能

<details>
<summary>点击展开 Phase 5 详情</summary>

### 后端新增
- [x] VO: DashboardOverviewVO (totalTasks, todayCheckins, todayRate, weekCheckins, latestWeight, weightDirection, weightMiniTrend, todayCalories, monthlyIncome/Expense/Balance)
- [x] VO: DashboardCalendarVO (days 含 date/checkinCount/hasIncome/hasExpense)
- [x] DashboardService: getOverview() 聚合 Tasks + Weights + Diets + Ledger; getCalendar() 按年月返回打卡日历
- [x] DashboardController: 2 个 REST 端点

### 后端修改
- [x] HealthService: 新增 updateWeight() 体重记录编辑 (所有权校验 + 部分字段更新)
- [x] HealthController: 新增 PUT /api/health/weights/{id} 编辑端点
- [x] DTO: 新增 UpdateWeightRecordDTO

### 前端新增
- [x] api/dashboard.js: 2 个 API 函数封装 (getOverview / getCalendar)
- [x] DashboardHome.vue 完全重写: 真实数据卡片 (今日打卡+环形进度/最新体重+方向/今日摄入/本月结余) + 3 个 ECharts 图表 (周打卡柱状图/体重迷你折线/月度收支对比) + 打卡迷你日历 (含月份切换 + 打卡/收支标记)

### 前端修改
- [x] api/health.js: 新增 updateWeight(id, data) 函数
- [x] HealthPage.vue: 体重表格增加编辑按钮 + 体重对话框支持编辑模式 (标题/按钮文字/API 调用自动切换) + 体重趋势图添加范围选择器 (近7/30/90/180天按钮组)
- [x] HealthPage.vue: 导入 Edit 图标

### API 验证 (全部通过)
- [x] PUT /api/health/weights/{id} → 200 (编辑体重记录)
- [x] GET /api/dashboard/overview → 200 (仪表盘总览数据完整)
- [x] GET /api/dashboard/calendar?year=2026&month=5 → 200 (5月日历含31天)
- [x] GET /api/health/weights/trend?days=7 → 200 (新增范围参数验证)

</details>

### Phase 5 补充功能

<details>
<summary>点击展开补充功能详情</summary>

### 1. 体重记录编辑功能
- [x] 后端: UpdateWeightRecordDTO + PUT /api/health/weights/{id}
- [x] 前端: 体重表格编辑按钮 + 对话框复用 (新增/编辑模式自动切换)

### 2. 体重趋势日期范围调节
- [x] 前端: 趋势图右上角范围按钮组 (近7天/近30天/近90天/近180天)
- [x] 后端: getWeightTrend() 已支持 days 参数，无需额外修改

</details>

### Phase 6 待实现 (部署与测试)

- [ ] 后端单元测试 + 集成测试
- [ ] 前端 E2E 测试
- [ ] Docker 容器化部署
- [ ] 生产环境配置 (MySQL 密码外部化, HTTPS, 域名)
