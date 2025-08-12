import unittest
from HtmlTestRunner import HTMLTestRunner
import os

report_dir = "test_reports"
if not os.path.exists(report_dir):
    os.makedirs(report_dir)

test_suite = unittest.defaultTestLoader.discover(
    start_dir="test_suite",
    pattern="test_*.py"
)

# 生成HTML报告
runner = HTMLTestRunner(
    output=report_dir,
    report_name="web_auto_test_report",
    report_title="web_auto_test_report",
    combine_reports=True,
    add_timestamp=True
)

# 执行测试并生成报告
runner.run(test_suite)