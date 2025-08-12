import requests
from config.config import Config
import allure
import logging

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class Request:
    @staticmethod
    @allure.step("发送请求：{method} {endpoint}")
    def send_request(method, endpoint, headers=None, params=None, json=None, files=None, token=None):
        # 1. 正确拼接URL
        base_url = Config.BASE_URL.rstrip('/')
        endpoint = endpoint.lstrip('/')
        url = f"{base_url}/{endpoint}"
        logger.info(f"请求URL: {url}")
        
        # 2. 构建请求头
        default_headers = {}
        
        if token:
            # 确保token有效且格式正确
            token_str = token.strip()
            if token_str:  # 只在token不为空时添加
                default_headers["Authorization"] = f"Bearer {token_str}"
                logger.info(f"使用Token: {token_str[:10]}...")  # 只打印部分token用于调试
        
        # 合并自定义headers
        if headers:
            default_headers.update(headers)
        
        # 文件上传时移除默认Content-Type
        if files:
            default_headers.pop("Content-Type", None)
        
        logger.info(f"请求头: {default_headers}")
        
        try:
            # 3. 发送请求
            response = requests.request(
                method=method.upper(),
                url=url,
                headers=default_headers,
                params=params,
                json=json,
                files=files,
                timeout=15
            )
            
            # 打印响应信息
            logger.info(f"响应状态码: {response.status_code}")
            logger.info(f"响应内容: {response.text[:500]}")  # 只打印前500字符
            
            # allure报告附加信息
            allure.attach(f"URL: {url}", "请求信息", allure.attachment_type.TEXT)
            allure.attach(f"Headers: {default_headers}", "请求头", allure.attachment_type.TEXT)
            allure.attach(f"参数: {json or params}", "请求参数", allure.attachment_type.JSON)
            allure.attach(f"状态码: {response.status_code}", "响应信息", allure.attachment_type.TEXT)
            allure.attach(response.text, "响应体", allure.attachment_type.JSON)
            
            return response
        except Exception as e:
            logger.error(f"请求异常: {str(e)}")
            allure.attach(str(e), "请求异常", allure.attachment_type.TEXT)
            raise
