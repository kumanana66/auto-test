class TestContext:
    """存储测试过程中的动态参数（如token、验证码等）"""
    def __init__(self):
        self.storage = {}

    def set(self, key, value):
        self.storage[key] = value

    def get(self, key, default=None):
        return self.storage.get(key, default)

    def clear(self):
        self.storage = {}

# 单例模式
context = TestContext()