import pytest
import allure
import tempfile
from utils.request import Request
from utils.context import context
from utils.db_utils import DBUtils
import random
import string
from PIL import Image
import os

@pytest.fixture(scope="session")
def base_url():
    from config.config import Config
    return Config.BASE_URL

@pytest.fixture(scope="function")
def auth_token():
    response = Request.send_request(
        "POST", "/auth/login",
        json={"username": "admin1155", "password": "admin1155"}
    )
    assert response.status_code == 200, "登录失败，无法获取有效token"
    token = response.json()["data"]["token"]
    context.set("token", token)
    print(f"登录响应内容：{response},token：{token}")
    return token

@pytest.fixture(scope="function")
def small_png_image():
    """生成2MB以内的PNG测试图片"""
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp_file:
        img = Image.new('RGB', (1000, 1000), color=(255, 0, 0))
        img.save(tmp_file, "PNG")
        tmp_path = tmp_file.name  # 获取临时文件路径
    
    yield tmp_path  # 提供文件路径给测试用例
    
    # 确保文件被删除
    if os.path.exists(tmp_path):
        try:
            os.remove(tmp_path)
        except PermissionError:
            # 强制删除
            if os.name == 'nt':
                os.system(f"del /f {tmp_path}")

@pytest.fixture(scope="function")
def large_png_image():
    """生成超过2MB的PNG测试图片"""
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp_file:
        img = Image.new('RGB', (3000, 3000), color=(0, 255, 0))  # 增大尺寸确保超过2MB
        img.save(tmp_file, "PNG")
        tmp_path = tmp_file.name
    
    yield tmp_path
    
    if os.path.exists(tmp_path):
        try:
            os.remove(tmp_path)
        except PermissionError:
            if os.name == 'nt':
                os.system(f"del /f {tmp_path}")

@pytest.fixture(scope="function")
def small_jpg_image():
    """生成2MB以内的JPG测试图片"""
    with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp_file:
        img = Image.new("RGB", (2000, 2000), color=(0, 0, 255))
        img.save(tmp_file, "JPEG", quality=80)
        tmp_path = tmp_file.name
    
    yield tmp_path
    
    if os.path.exists(tmp_path):
        try:
            os.remove(tmp_path)
        except PermissionError:
            if os.name == 'nt':
                os.system(f"del /f {tmp_path}")

@pytest.fixture(scope="function")
def large_jpg_image():
    """生成超过2MB的JPG测试图片"""
    with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp_file:
        img = Image.new("RGB", (4000, 4000), color=(255, 255, 0))
        img.save(tmp_file, "JPEG", quality=90)
        tmp_path = tmp_file.name
    
    yield tmp_path
    
    if os.path.exists(tmp_path):
        try:
            os.remove(tmp_path)
        except PermissionError:
            if os.name == 'nt':
                os.system(f"del /f {tmp_path}")

@pytest.fixture(scope="function")
def non_image_file():
    """生成非图片格式测试文件（TXT）"""
    with tempfile.NamedTemporaryFile(suffix=".txt", delete=False) as tmp_file:
        tmp_file.write("这是一个用于测试的文本文件，非图片格式。".encode())
        tmp_path = tmp_file.name
    
    yield tmp_path
    
    if os.path.exists(tmp_path):
        try:
            os.remove(tmp_path)
        except PermissionError:
            if os.name == 'nt':
                os.system(f"del /f {tmp_path}")


@pytest.fixture(scope="function")
def test_user():
    """创建测试用户"""
    username = "test_" + ''.join(random.choices(string.ascii_lowercase, k=6))
    email = f"{username}@example.com"
    password = "Test123456"

    # 发送验证码并断言成功
    send_response = Request.send_request(
        "POST", "/auth/send-verify-code", 
        params={"email": email}
    )
    assert send_response.status_code == 200, f"发送验证码失败: {send_response.text}"
    
    # 获取验证码并断言非空
    code = DBUtils.get_latest_verification_code(email)
    assert code is not None, f"未获取到{email}的验证码"

    # 注册并断言成功
    register_response = Request.send_request(
        "POST", "/auth/register", 
        json={
            "username": username,
            "password": password,
            "email": email,
            "verifyCode": code
        }
    )
    assert register_response.status_code == 200, f"注册失败: {register_response.text}"

    # 登录并获取token
    login_response = Request.send_request(
        "POST", "/auth/login", 
        json={"username": username, "password": password}
    )
    assert login_response.status_code == 200, f"登录失败: {login_response.text}"
    
    token = login_response.json()["data"]["token"]
    yield {"username": username, "email": email, "token": token}

    # 清理测试数据
    DBUtils.clean_test_data(email)
