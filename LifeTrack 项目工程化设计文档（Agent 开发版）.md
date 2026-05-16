# LifeTrack 项目工程化设计文档（Agent 开发版）

------

# 1. API 接口开发文档

# 1.1 API 基础规范

## Base URL

```text
http://localhost:8080/api
```

生产环境：

```text
https://api.lifetrack.com/api
```

------

## 统一请求头

| Header        | 说明             |
| ------------- | ---------------- |
| Authorization | Bearer Token     |
| Content-Type  | application/json |

示例：

```http
Authorization: Bearer eyJhbGciOiJIUzI1Ni...
```

------

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1710000000
}
```

------

## 状态码规范

| 状态码 | 说明       |
| ------ | ---------- |
| 200    | 成功       |
| 400    | 参数错误   |
| 401    | 未登录     |
| 403    | 无权限     |
| 404    | 资源不存在 |
| 500    | 系统错误   |

------

# 1.2 Auth 模块

# 用户注册

## POST `/api/auth/register`

### Request

```json
{
  "email": "test@example.com",
  "password": "123456",
  "code": "123456"
}
```

### Response

```json
{
  "code": 200,
  "message": "register success",
  "data": null
}
```

------

# 用户登录

## POST `/api/auth/login`

### Request

```json
{
  "email": "test@example.com",
  "password": "123456"
}
```

### Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt_token",
    "expire": 604800,
    "refreshToken": "refresh_token"
  }
}
```

------

# 获取用户信息

## GET `/api/user/profile`

### Response

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "nickname": "Jason",
    "avatar": "avatar.png",
    "height": 175,
    "targetWeight": 70
  }
}
```

------

# 1.3 Task 模块

# 创建任务

## POST `/api/tasks`

### Request

```json
{
  "title": "晨跑",
  "description": "每天跑步30分钟",
  "repeatType": 1,
  "cronExpr": "0 0 8 * * ?",
  "startDate": "2026-05-16",
  "endDate": "2026-12-31"
}
```

------

# 获取任务列表

## GET `/api/tasks?page=1&pageSize=20`

### Response

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "title": "晨跑",
        "status": 1,
        "checkinRate": 85
      }
    ],
    "total": 1
  }
}
```

------

# 任务打卡

## POST `/api/tasks/{id}/checkin`

### Request

```json
{
  "note": "完成5公里"
}
```

------

# 1.4 Health 模块

# 记录体重

## POST `/api/health/weight`

### Request

```json
{
  "weight": 78.5,
  "recordDate": "2026-05-16"
}
```

------

# 获取体重趋势

## GET `/api/health/weight/trend?days=30`

### Response

```json
{
  "code": 200,
  "data": [
    {
      "date": "2026-05-01",
      "weight": 80.2
    }
  ]
}
```

------

# 添加饮食记录

## POST `/api/health/diet`

### Request

```json
{
  "mealType": "BREAKFAST",
  "foodId": 1,
  "quantity": 2
}
```

------

# 1.5 Ledger 模块

# 添加账单

## POST `/api/ledger/records`

### Request

```json
{
  "type": 0,
  "amount": 25.5,
  "categoryId": 1,
  "payment": "wechat",
  "recordDate": "2026-05-16"
}
```

------

# 获取月度统计

## GET `/api/ledger/stats/monthly`

### Response

```json
{
  "income": 10000,
  "expense": 6000,
  "balance": 4000
}
```

------

# 2. 数据库详细设计文档

# 2.1 users

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    birthday DATE,
    gender TINYINT,
    height DECIMAL(5,2),
    target_weight DECIMAL(5,2),
    notify_email VARCHAR(100),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
);
```

------

# 2.2 tasks

```sql
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    repeat_type TINYINT NOT NULL,
    cron_expr VARCHAR(100),
    remind_time TIME,
    start_date DATE,
    end_date DATE,
    status TINYINT DEFAULT 1,
    streak_days INT DEFAULT 0,
    max_streak_days INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_user_status(user_id, status)
);
```

------

# 2.3 task_checkins

```sql
CREATE TABLE task_checkins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    note VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task_date(task_id, checkin_date)
);
```

------

# 2.4 weight_records

```sql
CREATE TABLE weight_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    weight_kg DECIMAL(5,2) NOT NULL,
    bmi DECIMAL(5,2),
    body_fat_rate DECIMAL(5,2),
    muscle_mass DECIMAL(5,2),
    record_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date(user_id, record_date)
);
```

------

# 2.5 diet_records

```sql
CREATE TABLE diet_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    meal_type VARCHAR(20),
    food_id BIGINT,
    quantity DECIMAL(10,2),
    calories DECIMAL(10,2),
    protein DECIMAL(10,2),
    fat DECIMAL(10,2),
    carbs DECIMAL(10,2),
    record_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

------

# 2.6 ledger_records

```sql
CREATE TABLE ledger_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type TINYINT NOT NULL,
    amount DECIMAL(10,2),
    category_id BIGINT,
    payment VARCHAR(20),
    note VARCHAR(255),
    record_date DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date(user_id, record_date)
);
```

------

# 3. OpenAPI 规范（Swagger）

# 3.1 Maven

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

------

# 3.2 OpenAPI 配置

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                .title("LifeTrack API")
                .version("1.0")
                .description("LifeTrack RESTful APIs"));
    }
}
```

------

# 3.3 Swagger 注解规范

```java
@Tag(name = "任务管理")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Operation(summary = "创建任务")
    @PostMapping
    public Result<TaskVO> createTask(
            @RequestBody @Valid CreateTaskDTO dto) {
        return Result.success();
    }
}
```

------

# 4. Agent 开发规范

# AI_AGENT_RULES.md

```markdown
# LifeTrack Agent Development Rules

## Architecture Rules

1. 禁止 Controller 编写业务逻辑
2. 所有业务逻辑必须位于 Service 层
3. Mapper 仅负责数据库访问
4. 所有 API 返回 Result<T>

## DTO/VO Rules

1. 禁止直接返回 Entity
2. Request 使用 DTO
3. Response 使用 VO

## Database Rules

1. 所有表必须包含：
   - created_at
   - updated_at
   - is_deleted

2. 所有删除采用软删除

3. 所有列表接口必须分页

## Security Rules

1. 所有接口必须校验 JWT
2. 密码必须 BCrypt 加密
3. 禁止日志输出密码

## Coding Rules

1. 使用 Lombok
2. 使用 LocalDateTime
3. 禁止硬编码
4. 常量统一 constants 包管理

## API Rules

1. RESTful 风格
2. 接口必须 Swagger 注解
3. 参数必须 @Valid 校验
4. 统一异常处理

## Naming Rules

DTO:
- CreateTaskDTO
- UpdateTaskDTO

VO:
- TaskVO
- UserProfileVO

Entity:
- Task
- User
```

------

# 5. 系统架构图

# 5.1 Mermaid 架构图

```mermaid
graph TD

A[Vue3 Frontend]
B[Nginx]
C[Spring Boot API]
D[MySQL]
E[Redis]
F[Quartz Scheduler]
G[SMTP Mail Server]

A --> B
B --> C
C --> D
C --> E
C --> F
F --> G
```

------

# 5.2 后端模块架构

```mermaid
graph TD

A[Controller]
B[Service]
C[Mapper]
D[MySQL]

A --> B
B --> C
C --> D
```

------

# 6. 时序图

# 6.1 用户登录时序图

```mermaid
sequenceDiagram

participant User
participant Frontend
participant Backend
participant MySQL

User->>Frontend: 输入账号密码
Frontend->>Backend: POST /login
Backend->>MySQL: 查询用户
MySQL-->>Backend: 用户信息
Backend-->>Frontend: JWT Token
Frontend-->>User: 登录成功
```

------

# 6.2 任务打卡时序图

```mermaid
sequenceDiagram

participant User
participant Frontend
participant Backend
participant MySQL

User->>Frontend: 点击打卡
Frontend->>Backend: POST /checkin
Backend->>MySQL: 保存记录
Backend->>MySQL: 更新连续打卡
Backend-->>Frontend: success
```

------

# 6.3 邮件提醒时序图

```mermaid
sequenceDiagram

participant Quartz
participant Backend
participant MySQL
participant SMTP
participant User

Quartz->>Backend: 触发定时任务
Backend->>MySQL: 查询待提醒任务
MySQL-->>Backend: 返回任务列表
Backend->>SMTP: 发送邮件
SMTP-->>User: 邮件提醒
```

------

