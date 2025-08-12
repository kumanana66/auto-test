import yaml
import random
import string
from pathlib import Path
from utils.context import context

class CaseLoader:
    @staticmethod
    def load_cases(file_path):
        """加载YAML用例并处理动态占位符"""
        file_path = Path(file_path)
        if not file_path.exists():
            raise FileNotFoundError(f"用例文件不存在：{file_path}")
        
        with open(file_path, 'r', encoding='utf-8') as f:
            cases = yaml.safe_load(f)
        
        return CaseLoader._replace_placeholders(cases)

    @staticmethod
    def _replace_placeholders(data):
        """递归替换用例中的动态参数"""
        if isinstance(data, dict):
            return {k: CaseLoader._replace_placeholders(v) for k, v in data.items()}
        elif isinstance(data, list):
            return [CaseLoader._replace_placeholders(item) for item in data]
        elif isinstance(data, str):
            # 处理随机字符串占位符
            if "{random}" in data:
                random_str = ''.join(random.choices(string.ascii_lowercase + string.digits, k=6))
                data = data.replace("{random}", random_str)
            # 从上下文获取token，确保token存在
            if "{token}" in data:
                token = context.get("token", "")
                if token:  # 只有当token存在时才替换
                    data = data.replace("{token}", token)
                else:
                    # 移除无效的Bearer占位符
                    data = data.replace("Bearer {token}", "")
            return data
        return data

    @staticmethod
    def get_cases_by_module(module_name, file_path="test_cases/unified_test_cases.yaml"):
        """按模块获取用例"""
        all_cases = CaseLoader.load_cases(file_path)
        return all_cases.get(module_name, [])
