import json

class TestCaseLoader:
    def __init__(self, case_file="test_case.json"):
        with open(case_file, "r", encoding="utf-8") as f:
            self.case_data = json.load(f)

    def get_auth_case(self, case_name):
        """获取用户认证模块指定名称的测试用例"""
        # 遍历user_auth下所有type分组，查找目标用例
        for type_group in self.case_data["user_auth"].values():
            if case_name in type_group:
                return type_group[case_name]
        raise KeyError(f"用户认证模块中未找到用例: {case_name}")

    def get_crawler_case(self, case_name):
        """获取爬虫任务模块指定名称的测试用例"""
        # 遍历crawler_task下所有type分组，查找目标用例
        for type_group in self.case_data["crawler_task"].values():
            if case_name in type_group:
                return type_group[case_name]
        raise KeyError(f"爬虫任务模块中未找到用例: {case_name}")

    def get_all_auth_cases_by_type(self, case_type):
        """按类型获取用户认证模块所有用例"""
        if case_type not in self.case_data["user_auth"]:
            return []
        return list(self.case_data["user_auth"][case_type].values())

    def get_all_crawler_cases_by_type(self, case_type):
        """按类型获取爬虫任务模块所有用例"""
        if case_type not in self.case_data["crawler_task"]:
            return []
        return list(self.case_data["crawler_task"][case_type].values())
