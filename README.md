# Automated Testing Framework for Web and API

This repository contains a comprehensive automated testing framework for a web application, including both Web UI automation and API automation tests. The framework is built to validate the functionality of a user authentication system and a crawler task management platform.


## Overview

The project consists of automated test suites for:
- **Web UI Testing**: Validates user interactions with the frontend interface using Selenium
- **API Testing**: Verifies backend API functionality using HTTP requests
- **Test Utilities**: Common functions for database operations, token management, and test data loading

The tests cover critical business flows including user registration, login, profile management, crawler task creation, status management, and data export.


## Technology Stack

| Component               | Tools/Libraries                  |
|-------------------------|----------------------------------|
| Web UI Automation       | Selenium, unittest               |
| API Automation          | Requests, pytest                 |
| Test Reporting          | HTMLTestRunner, Allure           |
| Database Interaction    | PyMySQL                         |
| Backend (Application)   | Spring Boot                      |
| Frontend (Application)  | Vue.js, Element Plus             |
| Configuration           | YAML, JSON                       |


## Prerequisites

- Python 3.8+
- Java 11+
- Node.js 14+ 
- MySQL 5.7+
- Chrome browser (matching ChromeDriver version)
- ChromeDriver (compatible with installed Chrome version)


## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-directory>
   ```

2. Install Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```

3. Configure environment settings:
   - Update database credentials in `utils/db_utils.py`
   - Set correct ChromeDriver path in `base_test.py`
   - Verify application URLs (frontend: `http://localhost:5173`, backend API: `http://localhost:8080/api`)


## Test Execution

### Web UI Tests

Run all web automation tests:
```bash
python -m unittest discover -s test_suite -p "test_*.py"
```

Generate HTML report:
```bash
python -m pytest test_suite --html=web_test_report.html
```


### API Tests

Run API automation tests:
```bash
pytest tests --alluredir=allure-results
```

Generate and view Allure report:
```bash
allure serve allure-results
```


## Test Coverage

### User Authentication Module
- User registration with validation (username, password, email format)
- Login functionality (success/failure scenarios)
- Password reset and modification
- Avatar upload (file size and format validation)
- Verification code handling (expiration, correctness)

### Crawler Task Module
- Task creation with required fields validation
- Task status management (pause/resume)
- Task filtering and search
- Data export functionality (Excel format)
- ASIN list validation


## Notes

- Ensure the frontend and backend applications are running before executing tests
- Chrome and ChromeDriver versions must be compatible
- Database should be initialized with required tables (see SQL scripts in backend)
- Test files (images, exports) are automatically cleaned up after test execution
- API tests require valid authentication tokens for protected endpoints

For any questions or suggestions, please submit an Issue or Pull Request.

# 自动化测试框架 - Web与API测试解决方案

该项目是一个集成了Web UI自动化测试和API自动化测试的完整测试框架，主要用于测试用户认证系统和爬虫任务管理平台。框架基于Python的`unittest`和`pytest`构建，结合Selenium实现Web界面交互，通过Requests库进行API测试，同时包含后端Spring Boot和前端Vue.js的相关实现，形成完整的测试闭环。


## 项目特点

### 1. 测试覆盖范围
- **Web UI自动化**：涵盖用户登录、注册、头像上传、密码修改、爬虫任务创建/暂停/删除/筛选、数据导出等核心业务流程
- **API自动化**：针对用户认证、任务管理等接口进行全量测试，包括正向用例和异常场景
- **数据驱动**：通过`test_case.json`管理测试数据，支持用例参数化和批量执行

### 2. 技术亮点
- **分层架构**：基于`unittest`的BaseTest封装公共操作，实现测试用例的复用和解耦
- **状态管理**：支持Token缓存与自动注入，减少重复登录操作，提升测试效率
- **文件处理**：自动生成测试用例所需的图片文件（不同大小、格式），并在测试后自动清理
- **数据库交互**：集成MySQL数据库操作，支持验证码获取、测试数据清理等场景
- **报告生成**：支持HTML测试报告和Allure报告，直观展示测试结果

### 3. 核心功能
- 用户认证模块：注册、登录、密码修改、头像上传（含格式/大小校验）
- 爬虫任务模块：任务创建、状态切换、数据筛选、Excel导出、数据分析与PPT生成
- 自动化支持：元素等待机制、异常处理、用例依赖管理、测试前置/后置处理


## 环境要求

| 依赖项                | 版本要求                     |
|-----------------------|------------------------------|
| Python                | 3.8+                        |
| Java                  | 11+                         |
| Node.js               | 14+                         |
| MySQL                 | 5.7+                        |
| Chrome浏览器          | 对应ChromeDriver版本         |
| ChromeDriver          | 与Chrome版本匹配             |

### Python依赖库
```
selenium==4.10.0
requests==2.31.0
pytest==7.4.0
pytest-html==3.2.0
allure-pytest==2.13.2
PyMySQL==1.1.0
Pillow==10.0.0
PyYAML==6.0.1
```


## 安装与配置

### 1. 克隆仓库
```bash
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name
```

### 2. 安装依赖
```bash
# 安装Python依赖
pip install -r requirements.txt

# 安装前端依赖
cd frontend
npm install

# 后端依赖通过Maven管理
cd backend
mvn clean install
```

### 3. 环境配置
1. **数据库配置**：修改`utils/db_utils.py`中的数据库连接信息，确保与本地MySQL匹配
   ```python
   self.db_config = {
       'host': 'localhost',
       'user': 'root',
       'password': 'your_password',
       'database': 'spring_demo',
       'port': 3306
   }
   ```

2. **ChromeDriver配置**：在`BaseTest`类中指定ChromeDriver路径
   ```python
   service = Service(executable_path="D:/tools/chromedriver.exe")  # 修改为实际路径
   ```

3. **服务地址配置**：确保测试代码中的前端、后端地址与本地服务一致
   ```python
   # 前端地址
   self.driver.get("http://localhost:5173/login")
   # 后端API地址
   self.api_base_url = "http://localhost:8080/api"
   ```


## 运行测试

### 1. Web UI自动化测试
```bash
# 运行所有Web测试用例
python -m unittest discover -s test_suite -p "test_*.py"

# 生成HTML报告
python -m pytest test_suite --html=report.html
```

### 2. API自动化测试
```bash
# 运行API测试用例
pytest tests/test_api_cases.py --alluredir=allure-results

# 生成Allure报告
allure serve allure-results
```

### 3. 测试范围说明
- **用户认证模块**：测试注册、登录、密码修改、头像上传等场景
- **爬虫任务模块**：测试任务创建、状态切换、数据筛选、导出等功能
- **异常场景覆盖**：包括无效验证码、密码错误、文件格式错误、权限不足等场景

## 测试用例说明

### Web UI测试核心场景
- **用户认证**：用户名/密码校验、验证码有效期、密码强度校验、头像上传大小/格式限制
- **爬虫任务**：ASIN列表格式校验、任务创建必填项校验、任务状态切换、数据导出格式验证

### API测试核心场景
- **接口功能**：注册接口参数校验、登录Token生成、任务CRUD接口正确性
- **异常处理**：接口权限控制、参数错误提示、重复提交防护


## 注意事项
1. 运行测试前需确保前端（`localhost:5173`）和后端（`localhost:8080`）服务已启动
2. Chrome浏览器版本需与ChromeDriver匹配，否则会导致浏览器启动失败
3. 测试过程中生成的临时文件（如图片、下载文件）会自动清理，无需手动处理
4. 数据库操作涉及测试数据写入，建议使用测试环境数据库，避免影响生产数据
5. 验证码处理依赖数据库查询，需确保测试环境的验证码表（`verification_code`）可正常访问

如有任何问题或建议，欢迎提交Issue或Pull Request。
