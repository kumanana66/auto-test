# allure generate allure-results -o allure-report --clean
# allure open allure-report
class Config:
    # 接口基础URL
    BASE_URL = "http://localhost:8080/api"
    
    # 数据库配置
    DB_HOST = "localhost"
    DB_NAME = "spring_demo"
    DB_USER = "root"
    DB_PASSWORD = "200166"