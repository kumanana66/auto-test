import pymysql
from pymysql import Error
from config.config import Config
import allure

class DBUtils:
    @staticmethod
    def get_db_connection():
        """建立数据库连接"""
        try:
            return pymysql.connect(
                host=Config.DB_HOST,
                database=Config.DB_NAME,
                user=Config.DB_USER,
                password=Config.DB_PASSWORD,
                cursorclass=pymysql.cursors.DictCursor
            )
        except Error as e:
            allure.attach(f"数据库连接失败: {str(e)}", "数据库异常", allure.attachment_type.TEXT)
            raise

    @staticmethod
    @allure.step("从数据库获取验证码：{email}")
    def get_latest_verification_code(email):
        """获取最新验证码"""
        connection = None
        try:
            connection = DBUtils.get_db_connection()
            with connection.cursor() as cursor:
                cursor.execute("""
                    SELECT code FROM verification_code 
                    WHERE email = %s ORDER BY create_time DESC LIMIT 1
                """, (email,))
                result = cursor.fetchone()
                return result["code"] if result else None
        finally:
            if connection:
                connection.close()

    @staticmethod
    @allure.step("清理测试数据：{email}")
    def clean_test_data(email):
        """清理测试产生的数据"""
        connection = None
        try:
            connection = DBUtils.get_db_connection()
            with connection.cursor() as cursor:
                cursor.execute("DELETE FROM users WHERE email = %s", (email,))
                cursor.execute("DELETE FROM verification_code WHERE email = %s", (email,))
                connection.commit()
        finally:
            if connection:
                connection.close()