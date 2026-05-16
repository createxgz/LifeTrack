# LifeTrack 开发问题记录

> 记录各阶段开发中遇到的问题及解决方案，供后续参考。

---

## Phase 2: 任务打卡模块 (2026-05-16)

### 后端开发

| # | 问题 | 原因 | 解决方案 | 影响范围 |
|---|------|------|----------|----------|
| 1 | `PaginationInnerInterceptor` 编译找不到类 | MyBatis-Plus 3.5.5+ 将分页拦截器移到了独立模块 `mybatis-plus-jsqlparser`，不再包含在 `mybatis-plus-spring-boot3-starter` 中 | 在 pom.xml 中显式添加 `mybatis-plus-jsqlparser` 依赖（版本与 starter 一致） | pom.xml, MybatisPlusConfig |
| 2 | LifetrackApplication.java 编译失败 — 文件末尾有垃圾字符 `111111` | 前次编辑遗留 | 删除垃圾字符 | LifetrackApplication.java |
| 3 | `MybatisPlusConfig` 需要同时配置分页插件和 MetaObjectHandler | 分页插件需用 `MybatisPlusInterceptor` + `PaginationInnerInterceptor(DbType.MYSQL)` | 添加 `mybatisPlusInterceptor()` Bean，与现有 `metaObjectHandler()` 并存 | MybatisPlusConfig.java |
| 4 | TaskService 中 `selectPage()` 需要分页插件才能生效 | MyBatis-Plus 分页依赖拦截器，否则 `Page` 参数被忽略 | 通过配置 `MybatisPlusInterceptor` 解决（见 #1） | TaskService.java |

### 前端开发

| # | 问题 | 原因 | 解决方案 | 影响范围 |
|---|------|------|----------|----------|
| 5 | `@/api/tasks` 导入解析失败 | vite.config.js 缺少 `@` 路径别名配置 | 添加 `resolve.alias: { '@': path.resolve(__dirname, 'src') }` | vite.config.js |
| 6 | Element Plus 图标 `Scale`, `DishDot`, `Wallet` 不存在 | 这些图标名称在 @element-plus/icons-vue 2.3.2 中不存在 | 替换为存在的图标：`Scale→Histogram`, `DishDot→KnifeFork`, `Wallet→Money` | Dashboard.vue, DashboardHome.vue |
| 7 | Vue Router 需从平铺结构改为嵌套路由 | Dashboard 改为侧边栏布局后需使用嵌套 `<router-view>` | 将 Dashboard 作为父路由（layout），DashboardHome/TaskList/TaskDetail 作为 children | router/index.js, Dashboard.vue |
| 8 | Dashboard 布局改造后旧首页内容需分离 | Dashboard 现在是布局组件，原 4 卡片占位内容需要独立组件 | 创建 DashboardHome.vue 作为仪表盘首页 | DashboardHome.vue (新增) |

### 调试 / 验证

| # | 问题 | 原因 | 解决方案 | 影响范围 |
|---|------|------|----------|----------|
| 9 | Task API 返回 500 但无法看到错误详情 | GlobalExceptionHandler 吞掉了异常详情，只返回"服务器内部错误" | 临时将 `e.getMessage()` 返回给客户端（仅开发调试用） | GlobalExceptionHandler.java |
| 10 | curl POST 中文 JSON 导致 `Invalid UTF-8 start byte 0xb2` | Windows git-bash 环境下 curl 对中文 JSON 编码有问题 | 使用 ASCII 纯英文内容测试；或使用 `--data-raw` + 正确 Content-Type header | 仅影响测试 |

---

## Phase 3: 健康记录模块 (2026-05-16)

### 后端开发

| # | 问题 | 原因 | 解决方案 | 影响范围 |
|---|------|------|----------|----------|
| 11 | `POST /api/health/diets` 返回 500: `Cannot deserialize LocalDateTime` | Jackson 默认只接受 ISO 8601 格式 `yyyy-MM-ddTHH:mm:ss`，不接受空格分隔的 `yyyy-MM-dd HH:mm:ss` | 在 CreateDietRecordDTO.recordTime 字段上添加 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` | CreateDietRecordDTO.java |

---

## Phase 1: 项目基础设施 + 用户认证 (之前记录)

详见 PROGRESS.md 中的 Phase 1 问题记录（6 项：XML 转义、MySQL 编码、表名映射、端口冲突、密码配置、@TableName 缺失）

---

## 经验总结

### 后端
1. **MyBatis-Plus 3.5.5+ 分页插件需要单独依赖** — 以后项目中如果用 MP 3.5.5+，记得检查 `mybatis-plus-jsqlparser` 是否已引入
2. **分页插件和自动填充可以共存于同一个 Config 类** — 分别声明两个 `@Bean` 即可
3. **软删除使用 MyBatis-Plus `@TableLogic`** — 自动在 WHERE 条件中加入 `is_deleted=0`，不需要手动处理

### 前端
1. **Vite 项目模板默认没有 `@` 别名** — 每次新建 Vite 项目记得在 `vite.config.js` 中配置 `resolve.alias`
2. **Element Plus 图标先在 node_modules 中确认名称后再使用** — 官方文档可能滞后，`grep` 检查最可靠
3. **路由从平铺改为嵌套时** — 父组件需要 `<router-view />` 渲染子路由内容

### 后端 (Phase 3 新增)
4. **Jackson LocalDateTime 反序列化格式** — 默认只接受 ISO 8601 (T分隔符)。如果前端可能发送空格分隔的日期时间，需要在 DTO 字段上加 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`。Alternatively, 前端统一使用 ISO 8601 格式。

### 后端 (Phase 4 新增)
5. **V1 迁移创建了 ledger_categories / ledger_budgets 表但未创建实体类** — 新增模块时先检查数据库表是否已有对应的 Java Entity，避免 `Table 'xxx' doesn't exist` 或 Mapper 缺失
6. **Ledger 分类列表需要合并系统默认 + 用户自定义** — 使用 `and(w -> w.isNull(Category::getUserId).or().eq(Category::getUserId, userId))` 一次查询搞定
7. **CSV 导出中文乱码** — 在 Blob 前添加 BOM (`﻿`)，Excel 才能正确识别 UTF-8 编码

### Phase 3.5: 体重时段增强
8. **Flyway 版本化迁移** — 新增字段使用新版本 V2 而非修改 V1，确保已有数据库平滑升级
9. **体重趋势多记录处理** — 一天多次记录时，按 timeSlot 排序 + putIfAbsent 取最早（早晨）体重用于趋势计算

### Phase 5: 仪表盘 + 补充功能 (2026-05-16)
10. **仪表盘聚合查询需跨 4 个模块** — DashboardService 注入 Task/Checkin/Weight/Diet/Ledger 共 5 个 Mapper，避免循环依赖采用独立的 Dashboard 模块
11. **前后端字段名需保持一致** — DashboardOverviewVO 的 weekCheckins 字段需在 VO、Service、前端 renderCheckinChart 三处一致使用
12. **Vue 对话框编辑模式复用** — 通过 weightEditId (null=新增, 非null=编辑) 控制对话框标题、按钮文案、API 调用，避免维护两套对话框代码
13. **健康模块编辑与新增共用对话框** — openWeightDialog(row?) 根据是否传入 row 决定填充或清空表单，submitWeight 根据 weightEditId 选择 addWeight 或 updateWeight
14. **Dashboard 迷你日历的星期计算** — JavaScript getDay() 返回 0=周日，需转换为 0=周一（西欧/中国习惯），startDow = (getDay() + 6) % 7

### 调试
1. **500 错误调试**：临时在 GlobalExceptionHandler 中返回 `e.getMessage()` 可以快速定位问题
2. **Windows curl 编码问题**：涉及中文的场景优先用 Postman / Swagger UI / 纯 ASCII 测试
3. **后端更新后未生效**：Windows 后台运行的 Java 进程需手动 taskkill 重启，mvn spring-boot:run 在端口占用时会静默失败
