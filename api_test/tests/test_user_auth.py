import pytest
import allure
import time
import logging
from utils.case_loader import CaseLoader
from utils.request import Request
from utils.test_utils import assert_response, preprocess_request
from utils.db_utils import DBUtils
from tests.conftest import test_user  # 引入测试用户fixture

logger = logging.getLogger(__name__)

CASES = CaseLoader.get_cases_by_module("user_auth")

# 验证码缓存字典，邮箱:验证码
verify_code_cache = {}


def need_verify_code(case):
    """判断用例是否需要发送验证码"""
    json_data = case.get("json", {})
    # 检查是否包含需要动态生成的验证码占位符
    has_verify_code = (json_data.get("verifyCode") == "{verify_code}") or (json_data.get("verificationCode") == "{verify_code}")
    return has_verify_code


def send_and_get_verify_code(email):
    """发送并获取验证码（增加重试机制）"""
    if email in verify_code_cache:
        return verify_code_cache[email]
    
    max_retries = 3
    for retry in range(max_retries):
        try:
            send_response = Request.send_request(
                "POST", "/auth/send-verify-code",
                params={"email": email}
            )
            logger.info(f"发送验证码（第{retry+1}次）到邮箱：{email}，响应状态码：{send_response.status_code}")
            
            # 校验发送结果
            assert_response(
                send_response, 
                200, 
                expected_success=True, 
            )
            break
        except AssertionError as e:
            if retry == max_retries - 1:  # 最后一次重试失败则抛出异常
                logger.error(f"发送验证码失败：{str(e)}")
                raise
            time.sleep(2)  # 重试间隔
    
    time.sleep(5)  # 缩短等待数据库写入时间（根据实际情况调整）
    
    code = DBUtils.get_latest_verification_code(email)
    assert code is not None, f"未获取到{email}的验证码（数据库中无记录）"
    verify_code_cache[email] = str(code)
    return verify_code_cache[email]


# 拆分文件fixture，仅文件上传用例需要
@pytest.fixture(scope="function")
def image_fixtures(small_jpg_image, large_jpg_image, small_png_image, large_png_image, non_image_file):
    return {
        "small_jpg_image": small_jpg_image,
        "large_jpg_image": large_jpg_image,
        "small_png_image": small_png_image,
        "large_png_image": large_png_image,
        "non_image_file": non_image_file
    }


@allure.feature("用户认证模块")
@pytest.mark.parametrize("case", CASES)
def test_user_auth(case, test_user, image_fixtures):
    allure.story(case["name"])
    allure.title(f"{case['case_id']}: {case['name']}")
    logger.info(f"开始执行用例: {case['case_id']} - {case['name']}")

    # 1. 文件映射（仅文件上传用例需要）
    file_map = {
        "test_avatar.jpg": image_fixtures["small_jpg_image"],
        "large_avatar.jpg": image_fixtures["large_jpg_image"],
        "test_avatar.png": image_fixtures["small_png_image"],
        "large_avatar.png": image_fixtures["large_png_image"],
        "test_file.txt": image_fixtures["non_image_file"]
    }

    # 2. 预处理请求
    request_data = preprocess_request(case, test_user["token"], file_map)

    # 3. 处理验证码场景（仅当用例需要时）
    json_data = request_data.get("json", {})
    if not isinstance(json_data, dict):
        json_data = {}
        logger.warning("json_data不是字典类型，已初始化为空字典")

    # 判断是否需要处理验证码
    if need_verify_code(case):
        # 处理verifyCode占位符
        if json_data.get("verifyCode") == "{verify_code}":
            email = json_data.get("email")
            assert email is not None, "请求中未包含email字段，无法获取验证码"
            verify_code = send_and_get_verify_code(email)
            json_data["verifyCode"] = verify_code
            logger.info(f"为邮箱 {email} 填充验证码: {verify_code}")
        
        # 处理verificationCode占位符
        if json_data.get("verificationCode") == "{verify_code}":
            email = json_data.get("securityEmail", test_user["email"])
            verify_code = send_and_get_verify_code(email)
            json_data["verificationCode"] = verify_code
            logger.info(f"为安全邮箱 {email} 填充验证码: {verify_code}")
    else:
        logger.info(f"用例{case['case_id']}不需要验证码，跳过发送逻辑")

    # 4. 处理未登录场景
    use_token = case["case_id"] != "auth_029"
    logger.info(f"是否使用Token: {use_token}")
    current_token = test_user["token"] if use_token else None

    # 5. 发送请求
    response = Request.send_request(
        method=case["method"],
        endpoint=case["url"],
        json=request_data.get("json"),
        params=request_data.get("params"),
        headers=request_data.get("headers"),
        files=request_data.get("files"),
        token=current_token
    )

    # 6. 断言处理
    try:
        assert_response(
            response,
            expected_code=case["expected_code"],
            expected_success=case.get("expected_success"),
            expected_message=case.get("expected_message")
        )
    except AssertionError as e:
        logger.error(f"用例断言失败: {str(e)}")
        allure.attach(f"响应内容: {response.text}", "失败详情", allure.attachment_type.TEXT)
        allure.attach(f"请求参数: {json_data}", "请求数据", allure.attachment_type.JSON)
        raise

    # 7. 确保文件句柄关闭
    if "files" in request_data:
        for file_key, file_obj in request_data["files"].items():
            try:
                if not file_obj.closed:
                    file_obj.close()
                    logger.info(f"关闭文件句柄: {file_key}")
            except Exception as e:
                logger.warning(f"关闭文件句柄失败: {str(e)}")

    # 8. 清理测试用户（避免脏数据）
    if not case["case_id"].startswith("auth_017"):  # 排除管理员登录用例
        DBUtils.clean_test_data(test_user["email"])
        logger.info(f"清理测试用户: {test_user['email']}")