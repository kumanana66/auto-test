import pymysql
from datetime import datetime

class DBUtils:
    def __init__(self):
        # 数据库连接配置，根据实际环境修改
        self.db_config = {
            'host': 'localhost',
            'user': 'root',
            'password': '200166',
            'database': 'spring_demo',
            'port': 3306,
            'charset': 'utf8mb4'
        }
        self.connection = None
        
    def connect(self):
        """建立数据库连接"""
        self.connection = pymysql.connect(**self.db_config)
        self.connection.autocommit(True)
        return self.connection
        
    def close(self):
        """关闭数据库连接"""
        if self.connection:
            self.connection.close()
            
    def get_latest_verification_code(self, email):
        """获取指定邮箱的最新验证码"""
        try:
            cursor = self.connection.cursor()
            # 查询指定邮箱未过期的最新验证码
            query = """
            SELECT code FROM verification_code
            WHERE email = %s
            ORDER BY create_time DESC
            LIMIT 1
            """

            cursor.execute(query, (email,))
            result = cursor.fetchone()
            return result[0] if result else None
        except Exception as e:
            print(f"获取验证码失败: {str(e)}")
            return None
        
    def clear_verification_codes(self, email=None):
        try:
            cursor = self.connection.cursor()
            if email:
                # 清除指定邮箱的验证码
                delete_query = "DELETE FROM verification_code WHERE email = %s"
                cursor.execute(delete_query, (email,))
                print(f"已清除邮箱 {email} 的所有验证码")
            else:
                # 清除所有验证码（谨慎使用，建议测试环境）
                delete_query = "DELETE FROM verification_code"
                cursor.execute(delete_query)
                print("已清除所有验证码")
            self.connection.commit()
        except Exception as e:
            print(f"清除验证码失败: {str(e)}")
            self.connection.rollback()