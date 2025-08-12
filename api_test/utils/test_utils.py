import allure
from utils.context import context

@allure.step("验证响应：状态码{expected_code}，消息{expected_message}")
def assert_response(response, expected_code, expected_success=None, expected_message=None, is_file=False):
    """
    新增is_file参数：标记是否为文件下载接口（无需JSON解析）
    """
    assert response.status_code == expected_code, \
        f"状态码不符：预期{expected_code}，实际{response.status_code}，完整response：{response.text}"

    if response.status_code != 200:
        return

    # 若为文件下载接口，无需解析JSON
    if is_file:
        return

    try:
        json_data = response.json()
    except:
        assert False, f"响应不是有效的JSON格式: {response.text}"
        
    if expected_success is not None:
        assert json_data.get("success") == expected_success, \
            f"success字段不符：预期{expected_success}，实际{json_data.get('success')}，完整response：{response.text}"
    
    if expected_message:
        assert json_data.get("message") == expected_message, \
            f"消息不符：预期{expected_message}，实际{json_data.get('message')}，完整response：{response.text}"

def preprocess_request(case, token, file_map=None):
    """预处理请求数据（替换动态参数参数）"""
    file_map = file_map or {}
    request_data = {
        "json": case.get("json", {}).copy(),  # 复制字典避免数据避免修改原用例
        "params": case.get("params", {}).copy(),
        "headers": case.get("headers", {}).copy()  # 复制headers避免修改原用例
    }

    # 移除用例中可能存在的Authorization头，避免与token参数冲突
    if "Authorization" in request_data["headers"]:
        del request_data["headers"]["Authorization"]

    # 处理文件上传
    if "files" in case:
        request_data["files"] = {k: open(file_map[v], "rb") for k, v in case["files"].items()}
    if "files" in request_data:
        request_data["headers"]["Content-Type"] = "multipart/form-data"
    
    return request_data
