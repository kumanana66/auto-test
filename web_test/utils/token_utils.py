import json
import os

class TokenUtils:
    TOKEN_FILE = "token.json"  # Token存储文件

    @classmethod
    def save_token(cls, token_data):
        """保存Token到本地文件"""
        with open(cls.TOKEN_FILE, "w", encoding="utf-8") as f:
            json.dump(token_data, f, ensure_ascii=False, indent=2)

    @classmethod
    def load_token(cls):
        """从本地文件加载Token"""
        if not os.path.exists(cls.TOKEN_FILE):
            return None
        try:
            with open(cls.TOKEN_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except (json.JSONDecodeError, IOError):
            return None

    @classmethod
    def delete_token(cls):
        """删除本地Token文件（失效时）"""
        if os.path.exists(cls.TOKEN_FILE):
            os.remove(cls.TOKEN_FILE)