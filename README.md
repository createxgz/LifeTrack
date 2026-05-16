<div align="center">

<img src="https://img.shields.io/badge/version-0.1.0--alpha-blue?style=flat-square" />
<img src="https://img.shields.io/badge/status-在建中-orange?style=flat-square" />
<img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" />
<img src="https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square" />

<br />
<br />

# 📋 LifeTrack

**个人任务打卡 · 健康记录 · 账本管理**

> 一个写给自己的生活管理系统 —— 因为真正适合自己的工具，只有自己最清楚。

<br />

[功能介绍](#-功能一览) · [技术栈](#-技术栈) · [快速开始](#-快速开始) · [路线图](#-开发路线图) · [文档](#-文档)

</div>

---

## 🌟 项目起源

减脂期间，我发现自己同时需要：

- 一个能**定时提醒我执行计划**的工具（不是备忘录，得推到我脸上）
- 一个**记录体重和饮食**的地方，能看到趋势曲线
- 一个**简单的账本**，知道钱花在哪里了

市面上的 App 各自为政，数据也不在自己手里。于是决定自己做一个。

目前正在持续开发中，功能会随着自己的需求不断迭代。

---

## ✨ 功能一览

### ✅ 任务打卡

- 创建个人计划，支持**每日 / 每周 / 每月 / 自定义间隔**多种重复模式
- **邮件自动提醒** —— 早间推送当日任务清单，晚上还没打卡会再催一次
- 连续打卡天数追踪，支持 3 天内**补签**
- 打卡热力图日历，一眼看到过去一年的坚持轨迹
- 连续打卡 7 / 30 / 100 天触发**里程碑邮件**🎉

### 🏋️ 健康记录

- 每日**体重记录**，自动计算 BMI，展示趋势折线图
- 设置目标体重，实时显示距目标的差距
- 按早 / 中 / 晚 / 加餐记录**饮食热量**
- 内置 500+ 常见食物热量库，支持自定义食物
- 每日热量摄入 vs 目标可视化，三大营养素占比分析

### 📒 账本管理

- 快速记录收支，支持自定义**分类与标签**
- 月度收支对比，支出分类占比饼图
- 为各类别设置**月度预算**，超支自动邮件提醒
- 近 6 个月收支趋势一览，支持导出 CSV

### 🏠 仪表盘

- 首页聚合今日任务、体重、热量、本月收支**核心数据**
- 所有关键指标一屏掌握，快速打卡不跳页

---

## 🛠 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Java 17 + Spring Boot 3.2 | 主框架 |
| Spring Security 6 + JWT | 无状态认证 |
| MyBatis-Plus 3.5 | ORM，支持软删除、分页 |
| Quartz Scheduler | 定时任务（邮件提醒调度） |
| Spring Mail + Thymeleaf | HTML 邮件渲染与发送 |
| MySQL 8.0 | 主数据库 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 + Vite 5 | 主框架，Composition API |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| ECharts 5 | 图表（折线/柱状/饼图/热力图） |
| Axios | HTTP 请求封装 |

### 部署

```
Docker + Docker Compose · Nginx 反向代理
```

---

## 🚀 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Docker（可选）

### 本地开发

```bash
# 克隆项目
git clone https://github.com/your-username/lifetrack.git
cd lifetrack

# 启动数据库（Docker）
docker-compose up -d mysql

# 后端
cd backend
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
# 编辑配置文件，填写数据库和邮件信息
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 前端（另开终端）
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`

### 环境变量说明

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=lifetrack
MYSQL_USERNAME=lifetrack
MYSQL_PASSWORD=your_password

JWT_SECRET=your_jwt_secret_32chars_minimum

# 邮件配置（以QQ邮箱为例）
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your@qq.com
MAIL_PASSWORD=your_smtp_authorization_code
```

---

## 📁 项目结构

```
lifetrack/
├── backend/                  # Spring Boot 后端
│   └── src/main/java/com/lifetrack/
│       ├── module/
│       │   ├── user/         # 用户 & 认证
│       │   ├── task/         # 任务打卡
│       │   ├── health/       # 健康记录
│       │   └── ledger/       # 账本
│       └── infra/
│           ├── email/        # 邮件服务
│           └── scheduler/    # Quartz 定时任务
├── frontend/                 # Vue 3 前端
│   └── src/
│       ├── views/            # 页面
│       ├── components/       # 公共组件
│       ├── stores/           # Pinia 状态
│       └── api/              # 接口封装
├── docs/                     # 设计文档
├── docker-compose.yml
└── nginx.conf
```

---

## 🗺 开发路线图

> 持续迭代中，以下为规划方向，顺序和内容可能随需求调整。

#### 当前阶段（v0.1 · 基础搭建）
- [x] 项目架构设计与文档
- [ ] 用户注册 / 登录 / JWT 认证
- [ ] 任务 CRUD + 打卡逻辑
- [ ] Quartz 邮件提醒接入

#### 近期计划（v0.2 · 核心功能）
- [ ] 健康记录：体重追踪 + 趋势图
- [ ] 健康记录：饮食热量记录 + 食物库
- [ ] 账本：收支记录 + 分类统计
- [ ] 账本：预算设置与超支提醒

#### 中期计划（v0.3 · 体验打磨）
- [ ] 仪表盘整合，数据卡片化展示
- [ ] 打卡热力图日历组件
- [ ] 图表全面接入（ECharts）
- [ ] 响应式适配，移动端可用

#### 未来规划（v1.0+）
- [ ] **微信小程序端**（复用现有 API，提供原生移动体验）
- [ ] 微信订阅消息提醒（替代 / 补充邮件提醒）
- [ ] 微信 / 支付宝账单 CSV 导入
- [ ] PWA 离线支持
- [ ] 数据导出与备份

---

## 📖 文档

| 文档 | 说明 |
|------|------|
| [系统需求分析](docs/01-requirements.md) | 功能需求、非功能需求、用户故事 |
| [技术架构设计](docs/02-architecture.md) | 分层设计、前后端规范、环境配置 |
| [数据库设计](docs/03-database.md) | 完整 DDL、表关系、索引策略 |
| [接口文档](docs/04-api.md) | 全量 REST API 定义 |
| [邮件提醒设计](docs/05-email-reminder.md) | Quartz 调度、模板设计、重试机制 |

---

## 🤝 关于这个项目

这是一个**自用项目**，边做边学，功能跟着实际需求走。

如果你也有类似的需求，欢迎：
- ⭐ 点个 Star 关注进展
- 🐛 提 Issue 聊想法或发现的问题
- 🍴 Fork 改造成适合自己的版本

---

## 📄 License

[MIT](LICENSE) · 随便用，但别删掉 License 文件

---

<div align="center">

**做适合自己的工具** · 持续更新中 🚧

</div>
