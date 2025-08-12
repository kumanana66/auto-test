import unittest
import requests
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from utils.case_loader import TestCaseLoader
from utils.db_utils import DBUtils
from utils.token_utils import TokenUtils

class BaseTest(unittest.TestCase):
    def setUp(self):
        chrome_options = Options()
        chrome_options.add_argument("--start-maximized")
        chrome_options.add_experimental_option("prefs", {
            "download.default_directory": "./downloads",
            "download.prompt_for_download": False
        })
        
        # 手动指定ChromeDriver路径
        service = Service(executable_path="D:/tools/chromedriver.exe")
        self.driver = webdriver.Chrome(service=service, options=chrome_options)  # 使用service参数
        self.driver.get("http://localhost:5173/login")
        self.wait = WebDriverWait(self.driver, 10)
        self.case_loader = TestCaseLoader()

        # 初始化数据库工具和API基础URL
        self.db_utils = DBUtils()
        self.api_base_url = "http://localhost:8080/api"
        self.db_utils.connect()

        self._init_login_state()

    def _init_login_state(self):
        """初始化登录状态：优先使用Token，无效则重新登录"""
        # 1. 尝试加载本地Token
        token_data = TokenUtils.load_token()
        
        if token_data:
            # 2. 注入Token到浏览器localStorage
            self.driver.get("http://localhost:5173")  # 先访问首页避免跨域问题
            self.driver.execute_script(
                f"localStorage.setItem('auth_token', '{token_data['auth_token']}');"
            )
            
            # 3. 验证Token有效性（访问需要登录的页面）
            self.driver.get("http://localhost:5173/profile")  # 个人中心（需登录）
            try:
                # 若3秒内未跳转到登录页，说明Token有效
                WebDriverWait(self.driver, 3).until(
                    EC.url_contains("/profile")
                )
                print("Token有效，复用登录状态")
                return
            except:
                print("Token无效，需要重新登录")
                TokenUtils.delete_token()  # 删除无效Token

        # 4. Token无效或不存在，执行完整登录流程
        self.login()
        # 登录成功后保存Token
        self._save_token_after_login()

    def _save_token_after_login(self):
        """登录成功后从localStorage提取Token并保存"""
        try:
            # 假设前端将Token存储在localStorage的'token'字段
            token = self.driver.execute_script("return localStorage.getItem('auth_token');")
            
            if token:
                TokenUtils.save_token({
                    "auth_token": token,
                })
                print("登录成功，已保存最新Token")
        except Exception as e:
            print(f"保存Token失败: {str(e)}")

    # 登录
    def login(self, username=None, password=None):
        if not username or not password:
            login_case = self.case_loader.get_auth_case("login_success")
            username = login_case["username"]
            password = login_case["password"]

        self.driver.get("http://localhost:5173/login")
        self.wait.until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "input[placeholder='请输入用户名']"))
        )

        self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入用户名']").send_keys(username)
        self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入密码']").send_keys(password)
        self.driver.find_element(By.CSS_SELECTOR, ".el-button.el-button--primary").click()

        # 验证登录成功
        notification = self.get_notification()
        print(f"提示消息：{notification}")
        self.assertEqual(notification, "登录成功")

    def tearDown(self):
        self.driver.quit()
        self.db_utils.close()

    # 获取Element Plus消息提示
    def get_notification(self):
        notification = self.wait.until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "[role='alert']"))
        ).text
        if not notification:
            notification = self.wait.until(
                EC.presence_of_element_located((By.CSS_SELECTOR, ".el-message__content"))
            ).text
        return notification
    
    def wait_for_notification(self, expected_message, max_attempts=30, interval=1):
        attempt = 0
        notification = None
        while attempt < max_attempts:
            notification = self.get_notification()
            # 匹配到预期消息则退出循环
            if notification == expected_message:
                break
            time.sleep(interval)
            attempt += 1
        return notification
    
    # 发送验证码
    def send_verification_code(self, email):
        """调用API发送验证码"""
        url = f"{self.api_base_url}/auth/send-verify-code"
        params = {"email": email}
        response = requests.post(url, params=params)
        return response.json()
        
    # 获取最新验证码
    def get_latest_verify_code(self, email):
        """从数据库获取最新验证码"""
        return self.db_utils.get_latest_verification_code(email)