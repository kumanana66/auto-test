import pytest
import allure
import time  # 新增：用于轮询等待
from utils.case_loader import CaseLoader
from utils.request import Request
from utils.test_utils import assert_response, preprocess_request
from utils.context import context

CASES = CaseLoader.get_cases_by_module("crawler_tasks")

@pytest.fixture(scope="function")
def task_id(auth_token):
    """创建测试任务并返回ID，测试结束后自动清理"""
    create_response = Request.send_request(
        "POST", "/crawler/tasks",
        json={
            "processName": f"测试任务_{context.get('random_str')}",
            "asinList": "B08XJ8J7SZ",
            "requiredInfo": ["price"],
            "platform": "Amazon",
            "timeCycle": "daily"
        },
        token=auth_token
    )
    assert_response(create_response, 200, expected_success=True)
    task_id = create_response.json()["data"]["id"]
    assert task_id is not None, "任务ID获取失败"
    
    yield task_id
    
    if task_id:
        Request.send_request("DELETE", f"/crawler/tasks/{task_id}", token=auth_token)

@allure.feature("爬虫任务模块")
@pytest.mark.parametrize("case", CASES)
def test_crawler_tasks(case, auth_token, task_id):
    allure.story(case["name"])
    allure.title(f"{case['case_id']}: {case['name']}")

    # 替换任务ID占位符
    if "{task_id}" in case["url"]:
        case["url"] = case["url"].replace("{task_id}", str(task_id))
        allure.attach(f"使用任务ID: {task_id}", "测试信息", allure.attachment_type.TEXT)

    # --------------------------
    # 关键修改：处理PPT下载的前置依赖
    # --------------------------
    # 1. 若当前是下载PPT的用例（task_017），先启动数据分析并等待完成
    if case["case_id"] == "task_017":
        allure.attach("开始执行数据分析数据分析前置操作", "前置依赖", allure.attachment_type.TEXT)
        
        # 1.1 启动数据分析
        analyze_response = Request.send_request(
            "POST", f"/crawler/tasks/{task_id}/analyze",  # 对应task_016的接口
            token=auth_token
        )
        assert_response(
            analyze_response, 
            200, 
            expected_success=True, 
            expected_message="操作成功"  # 匹配task_016的预期消息
        )
        allure.attach("数据分析已启动", "前置操作结果", allure.attachment_type.TEXT)

        # 1.2 轮询等待分析完成（复用同test_download_analyzed_ppt的逻辑）
        max_wait_seconds = 180
        check_interval = 5
        elapsed_seconds = 0
        status = "PROCESSING"
        
        while elapsed_seconds < max_wait_seconds and status == "PROCESSING":
            # 查询当前状态
            status_response = Request.send_request(
                "GET", f"/crawler/tasks/{task_id}/analyze/status",
                token=auth_token
            )
            assert_response(status_response, 200)
            status = status_response.json()["data"]
            allure.attach(f"当前分析状态: {status} (等待{elapsed_seconds}秒)", "状态轮询", allure.attachment_type.TEXT)
            
            if status == "PROCESSING":
                time.sleep(check_interval)
                elapsed_seconds += check_interval
        
        # 1.3 校验分析状态（必须完成才能继续下载）
        assert status in ["COMPLETED", "FAILED"], f"数据分析超时，最终状态：{status}"
        if status == "FAILED":
            pytest.fail("数据分析失败，无法下载PPT")  # 分析失败则标记用例失败
        allure.attach("数据分析已完成", "前置操作结果", allure.attachment_type.TEXT)

    # 预处理请求
    request_data = preprocess_request(case, auth_token)
    if "headers" in request_data and "Authorization" in request_data["headers"]:
        del request_data["headers"]["Authorization"]

    # 发送请求（对task_017而言，此时已确保分析完成）
    response = Request.send_request(
        method=case["method"],
        endpoint=case["url"],
        json=request_data.get("json"),
        params=request_data.get("params"),
        headers=request_data.get("headers"),
        token=auth_token
    )

    # 断言（增加PPT格式校验）
    try:
        assert_response(
            response,
            expected_code=case["expected_code"],
            expected_success=case.get("expected_success"),
            expected_message=case.get("expected_message"),
            is_file=bool(case.get("expected_content_type"))
        )
        # 额外校验PPT文件类型（对应case中的expected_content_type）
        if case.get("expected_content_type"):
            assert case["expected_content_type"] in response.headers["Content-Type"], \
                f"文件类型错误，预期: {case['expected_content_type']}, 实际: {response.headers['Content-Type']}"
    except AssertionError as e:
        allure.attach(f"响应内容: {response.text}", "断言失败详情", allure.attachment_type.TEXT)
        raise e
