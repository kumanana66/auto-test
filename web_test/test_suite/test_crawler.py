from base_test import BaseTest
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import os
import time

class TestCrawlerTask(BaseTest):
    """爬虫任务相关测试（按type分类执行）"""
    
    def test_01_create_task_cases(self):
        """执行所有任务创建类型的测试用例"""
        create_cases = self.case_loader.get_all_crawler_cases_by_type("task_create")
        
        for case in create_cases:
            print(f"当前用例：{case}")
            with self.subTest(case=case):
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//div[contains(@class, 'el-sub-menu')]/span[text()='爬虫管理']"
                    ))
                ).click()
                
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//ul[contains(@class, 'el-menu')]/li[text()='任务创建']"
                    ))
                ).click()
                
                self.wait.until(EC.url_contains("/crawler/tasks/create"))
                
                # 填写任务信息
                self.driver.find_element(
                    By.CSS_SELECTOR, "input[placeholder='请输入流程名称']"
                ).send_keys(case["process_name"])
                self.driver.find_element(
                    By.CSS_SELECTOR, "textarea[placeholder='请输入ASIN（使用、;,或回车分隔）']"
                ).send_keys(case["asin_list"])
                
                # 选择所需信息
                for info in case["required_info"]:
                    self.driver.find_element(
                        By.XPATH, f"//div[contains(@class, 'el-checkbox-group')]//input[@value='{info}']/ancestor::label"
                    ).click()
                
                # 选择平台和周期
                self.driver.find_element(
                    By.XPATH, f"//div[contains(@class, 'el-radio-group')]//input[@value='{case['platform']}']/ancestor::label"
                ).click()
                self.driver.find_element(
                    By.XPATH, f"//div[contains(@class, 'el-radio-group')]//input[@value='{case['time_cycle']}']/ancestor::label"
                ).click()
                
                # 提交
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//button[contains(@class, 'el-button--primary')]//span[text()='提交']"
                    ))
                ).click()
                
                notification = self.wait_for_notification(case["expected_message"])
                print(f"提示消息：{notification}")
                self.assertEqual(notification, case["expected_message"])
                
                # 验证跳转URL
                if "expected_url_contains" in case:
                    self.assertTrue(case["expected_url_contains"] in self.driver.current_url)

    def test_02_task_status_cases(self):
        """执行所有任务状态变更类型的测试用例"""
        status_cases = self.case_loader.get_all_crawler_cases_by_type("task_status")
        
        for case in status_cases:
            print(f"当前用例：{case}")
            with self.subTest(case=case):
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//div[contains(@class, 'el-sub-menu')]/span[text()='爬虫管理']"
                    ))
                ).click()
                
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//ul[contains(@class, 'el-menu')]/li[text()='任务列表']"
                    ))
                ).click()
                self.wait.until(EC.url_contains("/crawler/tasks"))
                
                task_row = self.wait.until(
                    EC.presence_of_element_located((By.XPATH, f"//tr[contains(., '{case['task_name_contains']}')]"))
                )
                
                if case["expected_status"] == "暂停":
                    task_row.find_element(By.CSS_SELECTOR, ".el-button--warning").click()
                elif case["expected_status"] == "恢复":
                    task_row.find_element(By.CSS_SELECTOR, ".el-button--success").click()
                
                notification = self.wait_for_notification(case["expected_message"])
                print(f"提示消息：{notification}")
                self.assertEqual(notification, case["expected_message"])
                
                max_attempts = 20
                interval = 1
                updated_task_row = None
                for _ in range(max_attempts):
                    updated_task_row = self.wait.until(
                        EC.presence_of_element_located((By.XPATH, f"//tr[contains(., '{case['task_name_contains']}')]"))
                    )
                    if case["expected_after_status"] in updated_task_row.text:
                        break
                    time.sleep(interval)

                self.assertTrue(case["expected_after_status"] in updated_task_row.text)

    def test_03_task_filter_cases(self):
        """执行所有任务状态变更类型的测试用例"""
        status_cases = self.case_loader.get_all_crawler_cases_by_type("task_filter")
        
        for case in status_cases:
            print(f"当前用例：{case}")
            with self.subTest(case=case):
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//div[contains(@class, 'el-sub-menu')]/span[text()='爬虫管理']"
                    ))
                ).click()
                
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//ul[contains(@class, 'el-menu')]/li[text()='任务列表']"
                    ))
                ).click()
                self.wait.until(EC.url_contains("/crawler/tasks"))

                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//span[text()='清除筛选']"
                    ))
                ).click()

                filter_conditions = {}

                if "filter_status" in case:
                    self.select_filter_option(
                        label_text="任务状态",
                        filter_value=case["filter_status"]
                    )
                    filter_conditions["status"] = case["filter_status"]
                
                if "filter_platform" in case:
                    self.select_filter_option(
                        label_text="执行平台",
                        filter_value=case["filter_platform"]
                    )
                    filter_conditions["platform"] = case["filter_platform"]
                
                if "filter_cycle" in case:
                    self.select_filter_option(
                        label_text="执行周期",
                        filter_value=case["filter_cycle"]
                    )
                    filter_conditions["cycle"] = case["filter_cycle"]
                
                time.sleep(2)
                self.verify_filter_results(filter_conditions)

    def select_filter_option(self, label_text, filter_value):
        selected_text = None
        count = 0
        max_count = 20
        
        if filter_value is not None:
            selected_text = self.wait.until(
                EC.presence_of_element_located((
                    By.XPATH, f'//label[text()="{label_text}"]/following-sibling::div//div[contains(@class, "el-select__selected-item")]/span'
                ))
            ).text
            
            while selected_text != filter_value and count < max_count:
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, f'//label[text()="{label_text}"]/following-sibling::div//i[contains(@class, "el-select__caret")]'
                    ))
                ).click()
                time.sleep(1)
                actions = ActionChains(self.driver)
                actions.send_keys(Keys.ARROW_DOWN)
                actions.send_keys(Keys.ENTER)
                actions.perform()
                
                selected_text = self.wait.until(
                    EC.presence_of_element_located((
                        By.XPATH, f'//label[text()="{label_text}"]/following-sibling::div//div[contains(@class, "el-select__selected-item")]/span'
                    ))
                ).text
                count += 1

    def verify_filter_results(self, filter_conditions):
        table_body = self.wait.until(
            EC.presence_of_element_located((
                By.XPATH, "//div[contains(@class, 'el-table__body-wrapper')]//table/tbody"
            ))
        )
        
        # 获取所有行
        rows = table_body.find_elements(By.XPATH, "./tr")
        self.assertGreater(len(rows), 0, "筛选结果为空，无法验证")
        
        # 遍历每一行验证筛选条件
        for row_idx, row in enumerate(rows, 1):
            # 验证任务状态（对应表格第6列，索引5）
            if "status" in filter_conditions:
                status_element = row.find_element(
                    By.XPATH, f"./td[6]//span[contains(@class, 'el-tag__content')]"
                )
                actual_status = status_element.text
                expected_status = filter_conditions["status"]
                self.assertEqual(
                    actual_status, expected_status,
                    f"第{row_idx}行状态不符：预期[{expected_status}]，实际[{actual_status}]"
                )
            
            # 验证执行平台（对应表格第4列，索引3）
            if "platform" in filter_conditions:
                platform_element = row.find_element(
                    By.XPATH, f"./td[4]//span[contains(@class, 'el-tag__content')]"
                )
                actual_platform = platform_element.text
                expected_platform = filter_conditions["platform"]
                self.assertEqual(
                    actual_platform, expected_platform,
                    f"第{row_idx}行平台不符：预期[{expected_platform}]，实际[{actual_platform}]"
                )
            
            # 验证执行周期（对应表格第5列，索引4）
            if "cycle" in filter_conditions:
                cycle_element = row.find_element(
                    By.XPATH, f"./td[5]//span[contains(@class, 'el-tag__content')]"
                )
                actual_cycle = cycle_element.text
                expected_cycle = filter_conditions["cycle"]
                self.assertEqual(
                    actual_cycle, expected_cycle,
                    f"第{row_idx}行周期不符：预期[{expected_cycle}]，实际[{actual_cycle}]"
                )
        
        # 所有验证通过
        print(f"筛选验证通过，共验证{len(rows)}行数据")

    def test_04_export_data_cases(self):
        """执行所有数据导出类型的测试用例"""
        export_cases = self.case_loader.get_all_crawler_cases_by_type("export_data")
        
        for case in export_cases:
            print(f"当前用例：{case}")
            with self.subTest(case=case):
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//div[contains(@class, 'el-sub-menu')]/span[text()='爬虫管理']"
                    ))
                ).click()
                
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//ul[contains(@class, 'el-menu')]/li[text()='任务列表']"
                    ))
                ).click()
                self.wait.until(EC.url_contains("/crawler/tasks"))
                
                # 进入任务详情
                task_row = self.wait.until(
                    EC.presence_of_element_located((
                        By.XPATH, f"//tr[contains(., '{case['task_name_contains']}')]"
                    ))
                )
                task_row.find_element(By.CSS_SELECTOR, ".el-button--primary").click()
                self.wait.until(EC.url_contains("/crawler/tasks/"))
                
                # 导出数据
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//button[contains(@class, 'el-button--primary')]//span[contains(., '导出数据')]"
                    ))
                ).click()
                
                # 选择导出类型
                self.wait.until(
                    EC.element_to_be_clickable((
                        By.XPATH, "//span[text()='价格与排名数据']"
                    ))
                ).click()

                notification = self.wait_for_notification(case["expected_message"])
                self.assertEqual(notification, case["expected_message"])
                
                # 验证下载文件
                time.sleep(3)
                download_dir = "D:/下载"
                files = os.listdir(download_dir)
                self.assertTrue(any(case["expected_file_contains"] in f for f in files))