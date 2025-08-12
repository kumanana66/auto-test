import time
import os
import requests
import tempfile
import numpy as np
from PIL import Image
from base_test import BaseTest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

class TestUserAuth(BaseTest):
    """用户认证相关测试（按type分类执行）"""
    
    def setUp(self):
        """测试前准备：生成所有需要的测试文件"""
        super().setUp()  # 调用父类的setUp方法
        self.test_files = {}  # 存储测试文件路径
        
        # 生成2MB以内的PNG图片
        self.test_files['small_png'] = self._create_image(
            size=(1000, 1000),
            color=(255, 0, 0),
            format='PNG',
            suffix='.png'
        )
        
        # 生成超过2MB的PNG图片
        self.test_files['large_png'] = self._create_image(
            size=(5000, 5000),  # 增大尺寸到5000×5000
            color=None,  # 不使用单一颜色
            format='PNG',
            suffix='.png',
            add_noise=True  # 新增参数：添加噪点增加文件大小
        )
        
        # 生成2MB以内的JPG图片
        self.test_files['small_jpg'] = self._create_image(
            size=(2000, 2000),
            color=(0, 0, 255),
            format='JPEG',
            suffix='.jpg',
            quality=80
        )
        
        # 生成超过2MB的JPG图片
        self.test_files['large_jpg'] = self._create_image(
            size=(6000, 6000),  # 增大尺寸到6000×6000
            color=(255, 255, 0),
            format='JPEG',
            suffix='.jpg',
            quality=100  # 最高质量压缩
        )
        
        # 生成非图片格式文件（TXT）
        self.test_files['txt_file'] = self._create_text_file()
        
    def tearDown(self):
        """测试后清理：删除所有生成的测试文件"""
        for file_path in self.test_files.values():
            if os.path.exists(file_path):
                try:
                    os.remove(file_path)
                except PermissionError:
                    # 针对Windows系统的强制删除
                    if os.name == 'nt':
                        os.system(f"del /f {file_path}")
        super().tearDown()  # 调用父类的tearDown方法
    
    def _create_image(self, size, color, format, suffix, quality=100, add_noise=False):
        """创建临时图片文件的工具方法"""
        with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp_file:
            # 创建图片
            img = Image.new('RGB', size, color=color)
            
            # 如果需要添加噪点（增加PNG文件大小）
            if add_noise and color is None:
                # 生成随机噪点数组
                arr = np.random.randint(0, 256, size=(*size, 3), dtype=np.uint8)
                img = Image.fromarray(arr)
            
            img.save(tmp_file, format, quality=quality)
            
            # 验证文件大小（调试用）
            file_size = os.path.getsize(tmp_file.name) / (1024 * 1024)  # MB
            print(f"生成{format}文件大小: {file_size:.2f}MB")
            return tmp_file.name
    
    def _create_text_file(self):
        """创建临时文本文件的工具方法"""
        with tempfile.NamedTemporaryFile(suffix=".txt", delete=False) as tmp_file:
            tmp_file.write("这是一个用于测试的文本文件，非图片格式。".encode())
            return tmp_file.name

    def test_01_register_cases(self):
        """执行所有注册类型的测试用例"""
        register_cases = self.case_loader.get_all_auth_cases_by_type("register")
        
        for case in register_cases:
            print(f"当前用例：{case}")
            with self.subTest(case=case):
                self.driver.get("http://localhost:5173/register")
                
                # 填写注册信息
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入用户名']").send_keys(case["username"])
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入邮箱']").send_keys(case["email"])

                if "verify_code" in case:
                    self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入验证码']").send_keys(case["verify_code"])
                else:
                    # 发送并获取验证码
                    send_code_url = "http://localhost:8080/api/auth/send-verify-code"
                    
                    print(f"查询验证码的邮箱：{case['email']}，类型：{type(case['email'])}")
                    requests.post(send_code_url, params={"email": case["email"]})
                    max_attempts = 10
                    interval = 1
                    verify_code = None
                    for _ in range(max_attempts):
                        verify_code = self.get_latest_verify_code(case["email"])
                        if verify_code:
                            print(f"获取到的验证码：{verify_code}")
                            break
                        time.sleep(interval)
                    self.assertIsNotNone(verify_code, "未获取到验证码")

                    self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入验证码']").send_keys(verify_code)
                
                
                # 填写密码信息
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入密码']").send_keys(case["password"])
                if "confirm_password" in case:
                    self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请再次输入密码']").send_keys(case["confirm_password"])
                else:
                    self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请再次输入密码']").send_keys(case["password"])
                
                # 提交表单
                self.driver.find_element(By.CSS_SELECTOR, ".el-button.el-button--primary").click()
                
                # 验证结果
                notification = self.wait_for_notification(case["expected_message"])
                print(f"提示消息：{notification}")
                self.assertEqual(notification, case["expected_message"])
                
                # 验证跳转URL
                if "expected_url_contains" in case:
                    self.assertTrue(case["expected_url_contains"] in self.driver.current_url)

    def test_02_register_out_of_date_verify_code_cases(self):
        """执行所有注册验证码过期类型的测试用例"""
        register_cases = self.case_loader.get_all_auth_cases_by_type("register_out_of_date_verify_code")
        
        for case in register_cases:
            print(f"当前用例：{case}")
            with self.subTest(case=case):
                self.driver.get("http://localhost:5173/register")
                
                # 填写注册信息
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入用户名']").send_keys(case["username"])
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入邮箱']").send_keys(case["email"])
                
                # 发送并获取验证码
                send_code_url = "http://localhost:8080/api/auth/send-verify-code"
                
                print(f"查询验证码的邮箱：{case['email']}，类型：{type(case['email'])}")
                requests.post(send_code_url, params={"email": case["email"]})
                max_attempts = 10
                interval = 1
                verify_code = None
                for _ in range(max_attempts):
                    verify_code = self.get_latest_verify_code(case["email"])
                    if verify_code:
                        print(f"获取到的验证码：{verify_code}")
                        break
                    time.sleep(interval)
                self.assertIsNotNone(verify_code, "未获取到验证码")
                
                time.sleep(40)
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入验证码']").send_keys(verify_code)
                
                # 填写密码信息
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入密码']").send_keys(case["password"])
                if "confirm_password" in case:
                    self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请再次输入密码']").send_keys(case["confirm_password"])
                else:
                    self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请再次输入密码']").send_keys(case["password"])
                
                # 提交表单
                self.driver.find_element(By.CSS_SELECTOR, ".el-button.el-button--primary").click()
                
                # 验证结果
                notification = self.wait_for_notification(case["expected_message"])
                print(f"提示消息：{notification}")
                self.assertEqual(notification, case["expected_message"])
                
                # 验证跳转URL
                if "expected_url_contains" in case:
                    self.assertTrue(case["expected_url_contains"] in self.driver.current_url)

    def test_03_login_cases(self):
        """执行所有登录类型的测试用例"""
        login_cases = self.case_loader.get_all_auth_cases_by_type("login")
        
        for case in login_cases:
            print(f"当前用例：{case}")
            with self.subTest(case=case):
                self.driver.get("http://localhost:5173/login")
                self.wait.until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, "input[placeholder='请输入用户名']"))
                )
                
                # 输入登录信息
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入用户名']").send_keys(case["username"])
                
                if "wrong_password_counts" in case:
                    for _ in range(5):
                        self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入密码']").send_keys(case["password"])
                        time.sleep(1)
                else:
                    self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入密码']").send_keys(case["password"])
                
                self.driver.find_element(By.CSS_SELECTOR, ".el-button.el-button--primary").click()
                
                # 验证结果
                notification = self.wait_for_notification(case["expected_message"])
                print(f"提示消息：{notification}")
                self.assertEqual(notification, case["expected_message"])

    def test_04_upload_avatar_cases(self):
        """执行所有头像上传类型的测试用例"""
        upload_cases = self.case_loader.get_all_auth_cases_by_type("upload_avatar")
        
        for case in upload_cases:
            print(f"当前用例：{case}")
            with self.subTest(case=case):
                # 进入个人中心
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//li[contains(@class, 'el-menu-item')]/span[text()='个人中心']"
                    ))
                ).click()
                self.wait.until(EC.url_contains("/profile"))
                
                # 进入基本信息
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//div[@aria-controls='pane-basic']"
                    ))
                ).click()

                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入当前密码']").send_keys(case["old_password"])
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入新密码']").send_keys(case["new_password"])
                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请确认新密码']").send_keys(case["new_password"])

                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//span[text()='发送验证码']"
                    ))
                ).click()
                # 发送并获取验证码
                send_code_url = "http://localhost:8080/api/auth/send-verify-code"
                
                print(f"查询验证码的邮箱：{case['email']}，类型：{type(case['email'])}")
                requests.post(send_code_url, params={"email": case["email"]})
                max_attempts = 10
                interval = 1
                verify_code = None
                for _ in range(max_attempts):
                    verify_code = self.get_latest_verify_code(case["email"])
                    if verify_code:
                        print(f"获取到的验证码：{verify_code}")
                        break
                    time.sleep(interval)
                self.assertIsNotNone(verify_code, "未获取到验证码")

                self.driver.find_element(By.CSS_SELECTOR, "input[placeholder='请输入验证码']").send_keys(verify_code)
                
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//span[text()='保存修改']"
                    ))
                ).click()

                # 验证结果
                notification = self.wait_for_notification(case["expected_message"])
                print(f"提示消息：{notification}")
                self.assertEqual(notification, case["expected_message"])

    def test_05_upload_password_cases(self):
        """执行所有头像上传类型的测试用例"""
        upload_cases = self.case_loader.get_all_auth_cases_by_type("upload_password")
        
        for case in upload_cases:
            print(f"当前用例：{case}")
            with self.subTest(case=case):
                # 进入个人中心
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//li[contains(@class, 'el-menu-item')]/span[text()='个人中心']"
                    ))
                ).click()
                self.wait.until(EC.url_contains("/profile"))

                # 进入安全设置
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//div[@aria-controls='pane-security']"
                    ))
                ).click()

                # 上传头像
                upload_input = self.wait.until(
                    EC.presence_of_element_located((
                        By.CSS_SELECTOR, "input.el-upload__input[type='file']"
                    ))
                )
                mapped_file_path = self._get_test_file_name(case["file_name"])
                upload_input.send_keys(mapped_file_path)
                
                # 验证结果
                notification = self.wait_for_notification(case["expected_message"])
                print(f"提示消息：{notification}")
                self.assertEqual(notification, case["expected_message"])

    def _get_test_file_name(self, file_name):
        """根据原始用例中的文件路径匹配对应的测试文件"""
        if "success_png_avatar.png" in file_name:
            return self.test_files['small_png']
        elif "success_jpg_avatar.jpg" in file_name:
            return self.test_files['small_jpg']
        elif "error_png_avatar.png" in file_name:
            return self.test_files['large_png']
        elif "error_jpg_avatar.png" in file_name:
            return self.test_files['large_jpg']
        elif "error_not_avatar.txt" in file_name:
            return self.test_files['txt_file']
        else:
            raise ValueError(f"未找到匹配的测试文件：{file_name}")