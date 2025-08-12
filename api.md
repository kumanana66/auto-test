# 接口文档

## 一、用户认证相关接口

### 1. 用户注册接口

#### 文档概述

- **接口名称**：用户注册
- **接口地址**：POST /api/auth/register
- **版本号**：v1.0.0
- **适用范围**：用户注册新账号

#### 请求参数

| 参数名     | 类型   | 是否必传 | 描述       | 示例值             | 格式要求                                  |
| ---------- | ------ | -------- | ---------- | ------------------ | ----------------------------------------- |
| username   | string | required | 用户名     | "test_user"        | 6-20 位字符                               |
| password   | string | required | 密码       | "Test123456"       | 8-20 位，至少包含大小写字母和数字中的两类 |
| email      | string | required | 邮箱       | "test@example.com" | 符合邮箱格式                              |
| verifyCode | string | required | 邮箱验证码 | "123456"           | 6 位数字                                  |

#### 请求头

- Content-Type: application/json

#### 请求体

```json
{
  "username": "test_user",
  "password": "Test123456",
  "email": "test@example.com",
  "verifyCode": "123456"
}
```

#### 响应格式

- **状态码**：

  - 200：注册成功
  - 400：参数错误或验证码错误
  - 500：服务器内部错误

- **响应数据结构**：

```json
{
  "success": true,
  "message": "注册成功",
  "data": null
}
```

#### 错误处理

| 错误情况             | 说明                                   |
| -------------------- | -------------------------------------- |
| 验证码错误或已过期   | 验证码错误或已超过有效期               |
| 用户名已被注册       | 该用户名已被其他用户使用               |
| 密码强度不足         | 密码需至少包含大小写字母和数字中的两类 |
| 注册失败，请稍后重试 | 服务器处理异常                         |

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/auth/register" \
-H "Content-Type: application/json" \
-d '{
  "username": "test_user",
  "password": "Test123456",
  "email": "test@example.com",
  "verifyCode": "123456"
}'
```

#### 其他说明

- **依赖关系**：需先调用发送验证码接口获取验证码
- **注意事项**：密码不能为常见弱密码

### 2. 用户登录接口

#### 文档概述

- **接口名称**：用户登录
- **接口地址**：POST /api/auth/login
- **版本号**：v1.0.0
- **适用范围**：用户登录系统

#### 请求参数

| 参数名   | 类型   | 是否必传 | 描述   | 示例值       | 格式要求    |
| -------- | ------ | -------- | ------ | ------------ | ----------- |
| username | string | required | 用户名 | "test_user"  | 6-20 位字符 |
| password | string | required | 密码   | "Test123456" | 8-20 位     |

#### 请求头

- Content-Type: application/json

#### 请求体

```json
{
  "username": "test_user",
  "password": "Test123456"
}
```

#### 响应格式

- **状态码**：

  - 200：登录成功
  - 401：用户名或密码错误、账号锁定
  - 403：密码错误

- **响应数据结构**：

```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  }
}
```

#### 错误处理

| 错误情况         | 说明                             |
| ---------------- | -------------------------------- |
| 用户名或密码错误 | 用户名不存在或密码不正确         |
| 账号已锁定       | 连续多次输入错误密码导致账号锁定 |

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
-H "Content-Type: application/json" \
-d '{
  "username": "test_user",
  "password": "Test123456"
}'
```

#### 其他说明

- **注意事项**：连续 5 次输入错误密码将导致账号锁定 30 分钟
- **依赖关系**：登录成功后获取的 token 需在后续请求中携带

### 3. 检查用户名是否存在接口

#### 文档概述

- **接口名称**：检查用户名是否存在
- **接口地址**：GET /api/auth/check-username
- **版本号**：v1.0.0
- **适用范围**：注册前检查用户名可用性

#### 请求参数

| 参数名   | 类型   | 是否必传 | 描述   | 示例值      | 格式要求    |
| -------- | ------ | -------- | ------ | ----------- | ----------- |
| username | string | required | 用户名 | "test_user" | 6-20 位字符 |

#### 请求头

- Content-Type: application/json

#### 响应格式

- **状态码**：

  - 200：查询成功

- **响应数据结构**：

```json
{
  "exists": false,
  "message": "用户名可用"
}
```

#### 调用示例

```bash
curl -X GET "http://localhost:8080/api/auth/check-username?username=test_user" \
-H "Content-Type: application/json"
```

### 4. 发送邮箱验证码接口

#### 文档概述

- **接口名称**：发送邮箱验证码
- **接口地址**：POST /api/auth/send-verify-code
- **版本号**：v1.0.0
- **适用范围**：注册、绑定邮箱等场景发送验证码

#### 请求参数

| 参数名 | 类型   | 是否必传 | 描述     | 示例值             | 格式要求     |
| ------ | ------ | -------- | -------- | ------------------ | ------------ |
| email  | string | required | 邮箱地址 | "test@example.com" | 符合邮箱格式 |

#### 请求头

- Content-Type: application/json

#### 响应格式

- **状态码**：

  - 200：发送成功
  - 500：发送失败

- **响应数据结构**：

```json
{
  "success": true,
  "message": "验证码已发送至您的邮箱",
  "data": null
}
```

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/auth/send-verify-code?email=test@example.com" \
-H "Content-Type: application/json"
```

#### 其他说明

- **注意事项**：验证码有效期为 5 分钟

### 5. 验证邮箱验证码接口

#### 文档概述

- **接口名称**：验证邮箱验证码
- **接口地址**：POST /api/auth/verify-email-code
- **版本号**：v1.0.0
- **适用范围**：验证邮箱验证码有效性

#### 请求参数

| 参数名 | 类型   | 是否必传 | 描述     | 示例值             | 格式要求     |
| ------ | ------ | -------- | -------- | ------------------ | ------------ |
| email  | string | required | 邮箱地址 | "test@example.com" | 符合邮箱格式 |
| code   | string | required | 验证码   | "123456"           | 6 位数字     |

#### 请求头

- Content-Type: application/json

#### 请求体

```json
{
  "email": "test@example.com",
  "code": "123456"
}
```

#### 响应格式

- **状态码**：

  - 200：验证成功
  - 400：验证码错误或已过期

- **响应数据结构**：

```json
{
  "success": true,
  "message": "验证成功",
  "data": true
}
```

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/auth/verify-email-code" \
-H "Content-Type: application/json" \
-d '{
  "email": "test@example.com",
  "code": "123456"
}'
```

### 6. 上传头像接口

#### 文档概述

- **接口名称**：上传头像
- **接口地址**：POST /api/auth/upload/avatar
- **版本号**：v1.0.0
- **适用范围**：已登录用户上传个人头像

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述     | 示例值 | 格式要求                      |
| ------ | ---- | -------- | -------- | ------ | ----------------------------- |
| file   | file | required | 头像文件 | -      | JPG、PNG 格式，大小不超过 2MB |

#### 请求头

- Content-Type: multipart/form-data
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：上传成功
  - 403：文件格式或大小不符合要求\未认证

- **响应数据结构**：

```json
{
  "success": true,
  "message": "操作成功",
  "data": "http://localhost:8080/avatars/test_user_1623456789.jpg"
}
```

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/auth/upload/avatar" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: multipart/form-data" \
-F "file=@/path/to/avatar.jpg"
```

#### 其他说明

- **注意事项**：仅支持 JPG、PNG 格式，文件大小不超过 2MB
- **依赖关系**：需要先登录获取 token

### 7. 获取用户信息接口

#### 文档概述

- **接口名称**：获取用户信息
- **接口地址**：GET /api/auth/userinfo
- **版本号**：v1.0.0
- **适用范围**：已登录用户获取个人信息

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：查询成功
  - 400：未认证

- **响应数据结构**：

```json
{
  "username": "test_user",
  "avatar": "http://localhost:8080/avatars/test_user_1623456789.jpg",
  "email": "test@example.com"
}
```

#### 调用示例

```bash
curl -X GET "http://localhost:8080/api/auth/userinfo" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

### 8. 更新用户信息接口

#### 文档概述

- **接口名称**：更新用户信息
- **接口地址**：PUT /api/auth/userinfo
- **版本号**：v1.0.0
- **适用范围**：已登录用户更新个人信息

#### 请求参数

| 参数名           | 类型   | 是否必传 | 描述       | 示例值            | 格式要求                                  |
| ---------------- | ------ | -------- | ---------- | ----------------- | ----------------------------------------- |
| securityEmail    | string | optional | 新邮箱地址 | "new@example.com" | 符合邮箱格式                              |
| verificationCode | string | optional | 邮箱验证码 | "123456"          | 6 位数字，与新邮箱配套                    |
| oldPassword      | string | optional | 旧密码     | "Test123456"      | 8-20 位                                   |
| newPassword      | string | optional | 新密码     | "NewTest123"      | 8-20 位，至少包含大小写字母和数字中的两类 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 请求体

```json
{
  "securityEmail": "new@example.com",
  "verificationCode": "123456",
  "oldPassword": "Test123456",
  "newPassword": "NewTest123"
}
```

#### 响应格式

- **状态码**：

  - 200：更新成功
  - 400：参数错误
  - 401：未认证

- **响应数据结构**：

```json
{
  "success": true,
  "message": "操作成功",
  "data": null
}
```

#### 错误处理

| 错误情况                 | 说明                     |
| ------------------------ | ------------------------ |
| 验证码错误或已过期       | 邮箱验证码不正确或已过期 |
| 该邮箱已被其他用户绑定   | 新邮箱已被其他用户使用   |
| 当前密码不正确           | 旧密码验证失败           |
| 新密码强度不足           | 新密码不符合安全要求     |
| 新密码不能与当前密码相同 | 新密码与旧密码一致       |

#### 调用示例

```bash
curl -X PUT "http://localhost:8080/api/auth/userinfo" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json" \
-d '{
  "securityEmail": "new@example.com",
  "verificationCode": "123456"
}'
```

#### 其他说明

- **注意事项**：可以只更新邮箱，或只更新密码，也可以同时更新
- **依赖关系**：更新邮箱需要先获取验证码

## 二、爬虫任务相关接口

### 1. 创建爬虫任务接口

#### 文档概述

- **接口名称**：创建爬虫任务
- **接口地址**：POST /api/crawler/tasks
- **版本号**：v1.0.0
- **适用范围**：已登录用户创建新的爬虫任务

/auth/register#### 请求参数

| 参数名       | 类型   | 是否必传 | 描述           | 示例值                  | 格式要求               |
| ------------ | ------ | -------- | -------------- | ----------------------- | ---------------------- |
| processName  | string | required | 任务名称       | "亚马逊商品监控"        | 非空                   |
| asinList     | string | required | ASIN 列表      | "B08XJ8J7SZ,B08LGD78Q5" | 逗号分隔的 ASIN 字符串 |
| requiredInfo | array  | required | 需要爬取的信息 | ["price", "review"]     | 数组元素为字符串       |
| platform     | string | required | 平台           | "Amazon"                | 非空                   |
| timeCycle    | string | required | 爬取周期       | "daily"                 | 非空                   |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 请求体

```json
{
  "processName": "亚马逊商品监控",
  "asinList": "B08XJ8J7SZ,B08LGD78Q5",
  "requiredInfo": ["price", "review"],
  "platform": "Amazon",
  "timeCycle": "daily"
}
```

#### 响应格式

- **状态码**：

  - 200：创建成功
  - 400：参数不全、参数错误、任务名重复
  - 401：未认证

- **响应数据结构**：

```json
{
  "success": true,
  "message": "任务创建成功",
  "data": {
    "id": 1,
    "processName": "亚马逊商品监控",
    "asinList": "B08XJ8J7SZ,B08LGD78Q5",
    "requiredInfo": ["price", "review"],
    "platform": "Amazon",
    "timeCycle": "daily",
    "createTime": "2023-06-15T10:30:00",
    "status": "ACTIVE",
    "userId": 1,
    "username": "test_user"
  }
}
```

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/crawler/tasks" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json" \
-d '{
  "processName": "亚马逊商品监控",
  "asinList": "B08XJ8J7SZ,B08LGD78Q5",
  "requiredInfo": ["price", "review"],
  "platform": "Amazon",
  "timeCycle": "daily"
}'
```

#### 其他说明

- **权限要求**：需要 USER 角色

- **依赖关系**：需要先登录获取 token

### 2. 保存任务为草稿接口

#### 文档概述

- **接口名称**：保存任务为草稿
- **接口地址**：POST /api/crawler/tasks/draft
- **版本号**：v1.0.0
- **适用范围**：已登录用户将任务保存为草稿

#### 请求参数

| 参数名       | 类型   | 是否必传 | 描述           | 示例值                  | 格式要求               |
| ------------ | ------ | -------- | -------------- | ----------------------- | ---------------------- |
| processName  | string | required | 任务名称       | "亚马逊商品监控"        | 非空                   |
| asinList     | string | required | ASIN 列表      | "B08XJ8J7SZ,B08LGD78Q5" | 逗号分隔的 ASIN 字符串 |
| requiredInfo | array  | required | 需要爬取的信息 | ["price", "review"]     | 数组元素为字符串       |
| platform     | string | required | 平台           | "Amazon"                | 非空                   |
| timeCycle    | string | required | 爬取周期       | "daily"                 | 非空                   |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 请求体

```json
{
  "processName": "亚马逊商品监控",
  "asinList": "B08XJ8J7SZ,B08LGD78Q5",
  "requiredInfo": ["price", "review"],
  "platform": "Amazon",
  "timeCycle": "daily"
}
```

#### 响应格式

- **状态码**：

  - 200：创建成功
  - 400：参数错误
  - 401：未认证
  - 403：权限不足

- **响应数据结构**：

```json
{
  "success": true,
  "message": "草稿保存成功",
  "data": {
    "id": 1,
    "processName": "亚马逊商品监控",
    "asinList": "B08XJ8J7SZ,B08LGD78Q5",
    "requiredInfo": ["price", "review"],
    "platform": "Amazon",
    "timeCycle": "daily",
    "createTime": "2023-06-15T10:30:00",
    "status": "DRAFT",
    "userId": 1,
    "username": "test_user"
  }
}
```

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/crawler/tasks/draft" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json" \
-d '{
  "processName": "亚马逊商品监控",
  "asinList": "B08XJ8J7SZ,B08LGD78Q5",
  "requiredInfo": ["price", "review"],
  "platform": "Amazon",
  "timeCycle": "daily"
}'
```

### 3. 获取任务列表接口

#### 文档概述

- **接口名称**：获取任务列表
- **接口地址**：GET /api/crawler/tasks
- **版本号**：v1.0.0
- **适用范围**：已登录用户获取自己创建的任务列表

#### 请求参数

| 参数名    | 类型   | 是否必传 | 描述       | 示例值     | 格式要求                                 |
| --------- | ------ | -------- | ---------- | ---------- | ---------------------------------------- |
| page      | int    | optional | 页码       | 1          | 默认为 1                                 |
| size      | int    | optional | 每页条数   | 10         | 默认为 10                                |
| status    | string | optional | 任务状态   | "ACTIVE"   | 可选值：ACTIVE, DRAFT, PAUSED, COMPLETED |
| timeCycle | string | optional | 时间周期   | "daily"    | 默认为空                                 |
| platform  | string | optional | 平台       | "Amazon"   | 默认为空                                 |
| keyword   | string | optional | 关键词搜索 | "商品监控" | 默认为空                                 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：查询成功
  - 401：未认证
  - 403：权限不足

- **响应数据结构**：

```json
{
  "success": true,
  "message": "任务列表获取成功",
  "data": {
    "content": [
      {
        "id": 1,
        "processName": "亚马逊商品监控",
        "asinList": "B08XJ8J7SZ,B08LGD78Q5",
        "requiredInfo": ["price", "review"],
        "platform": "Amazon",
        "timeCycle": "daily",
        "createTime": "2023-06-15T10:30:00",
        "status": "ACTIVE",
        "userId": 1,
        "username": "test_user"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0
  }
}
```

#### 调用示例

```bash
curl -X GET "http://localhost:8080/api/crawler/tasks?page=1&size=10&status=ACTIVE" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

### 4. 获取单个任务接口

#### 文档概述

- **接口名称**：获取单个任务
- **接口地址**：GET /api/crawler/tasks/{id}
- **版本号**：v1.0.0
- **适用范围**：已登录用户获取指定任务的详细信息

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述    | 示例值 | 格式要求 |
| ------ | ---- | -------- | ------- | ------ | -------- |
| id     | long | required | 任务 ID | 1      | 路径参数 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：查询成功
  - 401：未认证
  - 403：权限不足、任务不存在

- **响应数据结构**：

```json
{
  "success": true,
  "message": "任务获取成功",
  "data": {
    "id": 1,
    "processName": "亚马逊商品监控",
    "asinList": "B08XJ8J7SZ,B08LGD78Q5",
    "requiredInfo": ["price", "review"],
    "platform": "Amazon",
    "timeCycle": "daily",
    "createTime": "2023-06-15T10:30:00",
    "updateTime": "2023-06-15T11:30:00",
    "status": "ACTIVE",
    "userId": 1,
    "username": "test_user"
  }
}
```

#### 调用示例

```bash
curl -X GET "http://localhost:8080/api/crawler/tasks/1" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

### 5. 更新任务接口

#### 文档概述

- **接口名称**：更新任务
- **接口地址**：PUT /api/crawler/tasks/{id}
- **版本号**：v1.0.0
- **适用范围**：已登录用户更新自己创建的任务

#### 请求参数

| 参数名       | 类型   | 是否必传 | 描述           | 示例值                      | 格式要求                                 |
| ------------ | ------ | -------- | -------------- | --------------------------- | ---------------------------------------- |
| id           | long   | required | 任务 ID        | 1                           | 路径参数                                 |
| processName  | string | optional | 任务名称       | "亚马逊商品价格监控"        | 非空                                     |
| asinList     | string | optional | ASIN 列表      | "B08XJ8J7SZ,B08LGD78Q5"     | 逗号分隔的 ASIN 字符串                   |
| requiredInfo | array  | optional | 需要爬取的信息 | ["price", "review", "rank"] | 数组元素为字符串                         |
| platform     | string | optional | 平台           | "Amazon"                    | 非空                                     |
| timeCycle    | string | optional | 爬取周期       | "weekly"                    | 非空                                     |
| status       | string | optional | 任务状态       | "ACTIVE"                    | 可选值：ACTIVE, DRAFT, PAUSED, COMPLETED |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 请求体

```json
{
  "processName": "亚马逊商品价格监控",
  "asinList": "B08XJ8J7SZ,B08LGD78Q5,B091L5VJ6L",
  "requiredInfo": ["price", "review", "rank"],
  "platform": "Amazon",
  "timeCycle": "weekly",
  "status": "ACTIVE"
}
```

#### 响应格式

- **状态码**：

  - 200：更新成功
  - 400：参数错误
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在

- **响应数据结构**：

```json
{
  "success": true,
  "message": "任务更新成功",
  "data": {
    "id": 1,
    "processName": "亚马逊商品价格监控",
    "asinList": "B08XJ8J7SZ,B08LGD78Q5,B091L5VJ6L",
    "requiredInfo": ["price", "review", "rank"],
    "platform": "Amazon",
    "timeCycle": "weekly",
    "createTime": "2023-06-15T10:30:00",
    "updateTime": "2023-06-16T09:15:00",
    "status": "ACTIVE",
    "userId": 1,
    "username": "test_user"
  }
}
```

#### 调用示例

```bash
curl -X PUT "http://localhost:8080/api/crawler/tasks/1" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json" \
-d '{
  "processName": "亚马逊商品价格监控",
  "asinList": "B08XJ8J7SZ,B08LGD78Q5,B091L5VJ6L",
  "requiredInfo": ["price", "review", "rank"],
  "platform": "Amazon",
  "timeCycle": "weekly",
  "status": "ACTIVE"
}'
```

### 6. 删除任务接口

#### 文档概述

- **接口名称**：删除任务
- **接口地址**：DELETE /api/crawler/tasks/{id}
- **版本号**：v1.0.0
- **适用范围**：已登录用户删除自己创建的任务

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述    | 示例值 | 格式要求 |
| ------ | ---- | -------- | ------- | ------ | -------- |
| id     | long | required | 任务 ID | 1      | 路径参数 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：删除成功
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在

- **响应数据结构**：

```json
{
  "success": true,
  "message": "任务删除成功",
  "data": null
}
```

#### 调用示例

```bash
curl -X DELETE "http://localhost:8080/api/crawler/tasks/1" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

### 7. 暂停任务接口

#### 文档概述

- **接口名称**：暂停任务
- **接口地址**：POST /api/crawler/tasks/{id}/pause
- **版本号**：v1.0.0
- **适用范围**：已登录用户暂停自己创建的任务

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述    | 示例值 | 格式要求 |
| ------ | ---- | -------- | ------- | ------ | -------- |
| id     | long | required | 任务 ID | 1      | 路径参数 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：暂停成功
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在

- **响应数据结构**：

```json
{
  "success": true,
  "message": "操作成功",
  "data": null
}
```

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/crawler/tasks/1/pause" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

### 8. 恢复执行任务接口

#### 文档概述

- **接口名称**：恢复执行任务
- **接口地址**：POST /api/crawler/tasks/{id}/run
- **版本号**：v1.0.0
- **适用范围**：已登录用户恢复执行自己创建的暂停状态任务

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述    | 示例值 | 格式要求 |
| ------ | ---- | -------- | ------- | ------ | -------- |
| id     | long | required | 任务 ID | 1      | 路径参数 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：恢复成功
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在

- **响应数据结构**：

```json
{
  "success": true,
  "message": "操作成功",
  "data": null
}
```

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/crawler/tasks/1/run" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

### 9. 获取任务价格数据接口

#### 文档概述

- **接口名称**：获取任务价格数据
- **接口地址**：GET /api/crawler/tasks/{id}/price-ranks
- **版本号**：v1.0.0
- **适用范围**：已登录用户获取指定任务的价格和排名数据

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述    | 示例值 | 格式要求 |
| ------ | ---- | -------- | ------- | ------ | -------- |
| id     | long | required | 任务 ID | 1      | 路径参数 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：查询成功
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在

- **响应数据结构**：

```json
{
  "success": true,
  "message": "价格数据获取成功",
  "data": [
    {
      "id": 1,
      "asin": "B08XJ8J7SZ",
      "brand": "Test Brand",
      "originalPrice": 29.99,
      "ldDiscount": 0.0,
      "bdDiscount": 0.0,
      "memberPrice": 27.99,
      "memberFinalPrice": 27.99,
      "nonMemberFinalPrice": 29.99,
      "coupon": 0.0,
      "directDiscount": 0.0,
      "mainCategory": "Electronics",
      "mainCategoryRank": 1500,
      "subCategory": "Accessories",
      "subCategoryRank": 150,
      "crawlTime": "2023-06-15T14:30:00",
      "taskId": 1
    }
  ]
}
```

#### 调用示例

```bash
curl -X GET "http://localhost:8080/api/crawler/tasks/1/price-ranks" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

### 10. 获取任务评论数据接口

#### 文档概述

- **接口名称**：获取任务评论数据
- **接口地址**：GET /api/crawler/tasks/{id}/reviews
- **版本号**：v1.0.0
- **适用范围**：已登录用户获取指定任务的评论数据

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述    | 示例值 | 格式要求 |
| ------ | ---- | -------- | ------- | ------ | -------- |
| id     | long | required | 任务 ID | 1      | 路径参数 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：查询成功
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在

- **响应数据结构**：

```json
{
  "success": true,
  "message": "评论数据获取成功",
  "data": [
    {
      "id": 1,
      "asin": "B08XJ8J7SZ",
      "brand": "Test Brand",
      "reviewId": "R3X7ABCDEFGHIJ",
      "reviewerName": "John D.",
      "reviewTitle": "Great product!",
      "reviewContent": "I really like this product, it works well.",
      "reviewRating": 5.0,
      "reviewDate": "2023-06-10",
      "helpfulVotes": 5,
      "images": "",
      "crawlTime": "2023-06-15T14:30:00",
      "taskId": 1
    }
  ]
}
```

#### 调用示例

```bash
curl -X GET "http://localhost:8080/api/crawler/tasks/1/reviews" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

### 11. 导出任务数据接口

#### 文档概述

- **接口名称**：导出任务数据
- **接口地址**：GET /api/crawler/tasks/{id}/export
- **版本号**：v1.0.0
- **适用范围**：已登录用户导出指定任务的数据为 Excel 文件

#### 请求参数

| 参数名 | 类型   | 是否必传 | 描述         | 示例值       | 格式要求                    |
| ------ | ------ | -------- | ------------ | ------------ | --------------------------- |
| id     | long   | required | 任务 ID      | 1            | 路径参数                    |
| type   | string | required | 导出数据类型 | "price-rank" | 可选值：price-rank, reviews |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：导出成功
  - 400：参数错误
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在
  - 500：导出失败

- **响应数据**：
  Excel 文件，Content-Type 为 application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

#### 调用示例

```bash
curl -X GET "http://localhost:8080/api/crawler/tasks/1/export?type=price-rank" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json" \
-o "亚马逊商品监控_价格与排名数据_20230615.xlsx"
```

#### 其他说明

- **注意事项**：导出文件格式为 xlsx，包含任务相关的价格排名或评论数据

### 12. 分析任务数据接口

#### 文档概述

- **接口名称**：分析任务数据
- **接口地址**：POST /api/crawler/tasks/{id}/analyze
- **版本号**：v1.0.0
- **适用范围**：已登录用户对指定任务的数据进行分析，生成 PPT 报告

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述    | 示例值 | 格式要求 |
| ------ | ---- | -------- | ------- | ------ | -------- |
| id     | long | required | 任务 ID | 1      | 路径参数 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：分析启动成功
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在

- **响应数据结构**：

```json
{
  "success": true,
  "message": "操作成功",
  "data": null
}
```

#### 调用示例

```bash
curl -X POST "http://localhost:8080/api/crawler/tasks/1/analyze" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

#### 其他说明

- **注意事项**：数据分析为异步操作，需通过查询分析状态接口确认分析是否完成后再下载 PPT
- **依赖关系**：需要任务有足够的价格和评论数据

### 13. 下载分析后的 PPT 接口

#### 文档概述

- **接口名称**：下载分析后的 PPT
- **接口地址**：GET /api/crawler/tasks/{id}/analyze/ppt
- **版本号**：v1.0.0
- **适用范围**：已登录用户下载指定任务的数据分析 PPT 报告

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述    | 示例值 | 格式要求 |
| ------ | ---- | -------- | ------- | ------ | -------- |
| id     | long | required | 任务 ID | 1      | 路径参数 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：下载成功
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在或 PPT 文件不存在
  - 500：下载失败

- **响应数据**：
  PPT 文件，Content-Type 为 application/vnd.openxmlformats-officedocument.presentationml.presentation

#### 调用示例

```bash
curl -X GET "http://localhost:8080/api/crawler/tasks/1/analyze/ppt" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json" \
-o "数据分析报告_1_20230615.pptx"
```

#### 其他说明

- **依赖关系**：需要先调用分析任务数据接口并等待分析完成

### 14. 查询数据分析状态接口

#### 文档概述

- **接口名称**：查询数据分析状态
- **接口地址**：GET /api/crawler/tasks/{id}/analyze/status
- **版本号**：v1.0.0
- **适用范围**：已登录用户查询指定任务的数据分析状态

#### 请求参数

| 参数名 | 类型 | 是否必传 | 描述    | 示例值 | 格式要求 |
| ------ | ---- | -------- | ------- | ------ | -------- |
| id     | long | required | 任务 ID | 1      | 路径参数 |

#### 请求头

- Content-Type: application/json
- Authorization: Bearer {token}

#### 响应格式

- **状态码**：

  - 200：查询成功
  - 401：未认证
  - 403：权限不足
  - 404：任务不存在

- **响应数据结构**：

```json
{
  "success": true,
  "message": "查询状态成功",
  "data": "COMPLETED"
}
```

#### 状态说明

| 状态值     | 说明           |
| ---------- | -------------- |
| PROCESSING | 数据分析处理中 |
| COMPLETED  | 数据分析已完成 |
| FAILED     | 数据分析失败   |

#### 调用示例

```bash
curl -X GET "http://localhost:8080/api/crawler/tasks/1/analyze/status" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
-H "Content-Type: application/json"
```

## 五、通用错误码说明

| 错误码 | 含义                     | 说明                             |
| ------ | ------------------------ | -------------------------------- |
| 400    | 参数错误                 | 请求参数格式不正确或缺失必要参数 |
| 401    | 未认证                   | 需要登录后才能访问该接口         |
| 403    | 权限不足                 | 没有访问该接口的权限             |
| 404    | 资源不存在               | 请求的资源不存在                 |
| 500    | 服务器内部错误           | 服务器处理请求时发生错误         |
| 40001  | 验证码错误或已过期       | 邮箱验证码不正确或已超过有效期   |
| 40002  | 用户名已被注册           | 注册时用户名已存在               |
| 40003  | 密码强度不足             | 密码不符合安全要求               |
| 40004  | 密码为常见弱密码         | 密码在常见弱密码列表中           |
| 40005  | 用户名或密码错误         | 登录时用户名不存在或密码不正确   |
| 40006  | 账号已锁定               | 连续多次输入错误密码导致账号锁定 |
| 40007  | 当前密码不正确           | 更新密码时旧密码验证失败         |
| 40008  | 新密码不能与当前密码相同 | 新密码与旧密码一致               |
| 40009  | 该邮箱已被其他用户绑定   | 绑定邮箱时，邮箱已被其他用户使用 |

## 四、通用请求头说明

| 请求头名称    | 说明             | 示例值                                         | 是否必传     |
| ------------- | ---------------- | ---------------------------------------------- | ------------ |
| Content-Type  | 请求体的媒体类型 | application/json                               | 是           |
| Authorization | 认证令牌         | Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... | 仅需认证接口 |

## 五、通用响应格式说明

所有接口的响应格式统一为 JSON，结构如下：

```json
{
  "success": true/false,
  "message": "操作结果描述信息",
  "data": {} // 响应数据，根据接口不同而不同，成功时可能有数据，失败时可能为null
}
```

- **success**：布尔值，表示操作是否成功
- **message**：字符串，描述操作结果的信息
- **data**：任意类型，根据接口不同返回不同的数据，成功的操作可能返回业务数据，失败的操作可能为 null

## 六、注意事项

1. 所有需要认证的接口都需要在请求头中携带有效的 Authorization 令牌
2. 文件上传接口的 Content-Type 为 multipart/form-data，其他接口一般为 application/json
3. 接口返回的日期时间格式为 ISO 8601 格式，如：2023-06-15T14:30:00
4. 分页查询时，page 参数从 1 开始，size 参数表示每页条数
5. 密码要求至少包含大小写字母和数字中的两类，长度在 8-20 位之间
6. 邮箱验证码的有效期为 5 分钟
7. 连续 5 次输入错误密码将导致账号锁定 30 分钟
8. 头像上传支持 JPG、PNG 格式，文件大小不超过 2MB
9. 数据分析接口为异步操作，需要通过查询状态接口确认分析是否完成后再下载 PPT
10. 导出数据和生成的 PPT 文件存储在服务器本地，定期清理，请及时下载

## 七、更新历史

| 版本号 | 更新时间   | 更新内容                                 |
| ------ | ---------- | ---------------------------------------- |
| v1.0.0 | 2023-06-15 | 初始版本，包含用户认证和爬虫任务相关接口 |
