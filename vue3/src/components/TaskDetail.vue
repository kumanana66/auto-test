<!-- TaskDetail.vue -->
<template>
  <div class="task-detail-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-loading :fullscreen="true" :lock="true" text="加载中..." spinner="el-icon-loading"></el-loading>
    </div>

    <div v-else>
      <!-- 任务基本信息 -->
      <el-card class="task-info-card">
        <template #header>
          <div class="clearfix">
            <span>{{ task.processName }} - 任务详情</span>
            <div class="btn-group" style="float: right">
              <!-- 导出按钮 -->
              <el-button type="primary"  @click="exportData" style="margin-right: 10px">
                <i class="el-icon-download"></i> 导出数据
              </el-button>
              <el-button type="text"  @click="goBack">返回</el-button>
            </div>

          </div>
        </template>
        <div class="grid-content bg-purple">
          <el-row :gutter="20">
            <!-- 左侧四宫格区域 (占12列) -->
            <el-col :span="12">
              <!-- 第一行：平台、执行周期 -->
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-card class="info-item">
                    <div slot="header" class="clearfix">
                      <span>平台</span>
                    </div>
                    <el-tag :type="getPlatformType(task.platform)">{{ task.platform }}</el-tag>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card class="info-item">
                    <div slot="header" class="clearfix">
                      <span>执行周期</span>
                    </div>
                    <el-tag>{{ getCycleText(task.timeCycle) }}</el-tag>
                  </el-card>
                </el-col>
              </el-row>

              <!-- 第二行：任务状态、创建时间 -->
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card class="info-item">
                    <div slot="header" class="clearfix">
                      <span>任务状态</span>
                    </div>
                    <el-tag :type="getStatusType(task.status)">{{ getStatusText(task.status) }}</el-tag>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card class="info-item">
                    <div slot="header" class="clearfix">
                      <span>创建时间</span>
                    </div>
                    <el-tag>{{ $formatDate(task.createTime) }}</el-tag>
                  </el-card>
                </el-col>
              </el-row>
            </el-col>

            <!-- 右侧 ASIN 列表 (占12列) -->
            <el-col :span="12">
              <el-card class="info-item">
                <div slot="header" class="clearfix">
                  <span>ASIN列表</span>
                </div>
                <el-collapse>
                  <el-collapse-item title="查看全部 ASIN">
                    <div v-for="(asin, index) in asinList" :key="index" class="asin-item">
                      {{ asin }}
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 数据选项卡 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="价格与排名数据" name="price-rank">
          <el-table
              :data="priceRanks"
              stripe
              style="width: 100%"
              @row-click="handlePriceRowClick"
          >
            <el-table-column prop="asin" label="ASIN" min-width="120" />
            <el-table-column prop="brand" label="品牌" min-width="100" />
            <el-table-column prop="originalPrice" label="原价" min-width="70" />
            <el-table-column prop="ldDiscount" label="LD折扣" min-width="75" />
            <el-table-column prop="bdDiscount" label="BD折扣" min-width="75" />
            <el-table-column prop="coupon" label="优惠券" min-width="75" />
            <el-table-column prop="directDiscount" label="直降%" min-width="75" />
            <el-table-column prop="memberPrice" label="会员价" min-width="70" />
            <el-table-column prop="memberFinalPrice" label="会员最终价" min-width="70" />
            <el-table-column prop="nonMemberFinalPrice" label="非会员最终价" min-width="70" />
            <el-table-column prop="mainCategory" label="大类目" min-width="100" />
            <el-table-column prop="mainCategoryRank" label="大类目排名" min-width="70" />
            <el-table-column prop="subCategory" label="小类目" min-width="100" />
            <el-table-column prop="subCategoryRank" label="小类目排名" min-width="70" />
            <el-table-column prop="crawlTime" label="爬取时间" min-width="110" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="评论数据" name="reviews">
          <el-table
              :data="reviews"
              stripe
              style="width: 100%"
              @row-click="handleReviewRowClick"
          >
            <el-table-column prop="asin" label="ASIN" min-width="50" />
            <el-table-column prop="brand" label="品牌" min-width="80" />
            <el-table-column prop="reviewerName" label="评论者" min-width="100" />
            <el-table-column prop="reviewDate" label="评论日期" min-width="100" />
            <el-table-column label="评论内容" min-width="300">
              <template #default="{ row }">
                <el-popover placement="top-start" width="400" trigger="hover">
                  <p>{{ row.reviewContent }}</p>
                  <template #reference>
                    <span class="review-content-preview">{{ row.reviewContent.substring(0, 30) }}...</span>
                  </template>
                </el-popover>
              </template>
            </el-table-column>
            <el-table-column prop="crawlTime" label="爬取时间" min-width="100" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus'; // 显式导入ElMessageBox
import {
  getCrawlerTask,
  getTaskPriceRanks,
  getTaskReviews,
  exportTaskData // 数据导出接口
} from '../api/crawler';
import { saveAs } from 'file-saver';  // 文件下载

const router = useRouter();
const route = useRoute();

// 状态管理
const loading = ref(true);
const task = ref({});
const priceRanks = ref([]);
const reviews = ref([]);
const activeTab = ref('price-rank');
const exportLoading = ref(false);  // 导出加载状态

// 初始化
onMounted(async () => {
  await fetchTaskDetails();
});

// 获取任务详情
const fetchTaskDetails = async () => {
  const taskId = route.params.id;

  try {
    // 获取任务基本信息
    const taskResponse = await getCrawlerTask(taskId);
    if (taskResponse && taskResponse.success) {
      task.value = taskResponse.data;
    } else {
      ElMessage.error(taskResponse.message || '获取任务信息失败');
      router.push('/crawler/tasks');
      return;
    }

    // 获取价格与排名数据
    const priceResponse = await getTaskPriceRanks(taskId);
    if (priceResponse && priceResponse.success) {
      priceRanks.value = priceResponse.data;
    } else {
      ElMessage.error(priceResponse.message || '获取价格数据失败');
    }

    // 获取评论数据
    const reviewResponse = await getTaskReviews(taskId);
    if (reviewResponse && reviewResponse.success) {
      reviews.value = reviewResponse.data;
    } else {
      ElMessage.error(reviewResponse.message || '获取评论数据失败');
    }
  } catch (error) {
    console.error('获取任务详情异常', error);
    ElMessage.error('获取任务详情失败，请稍后重试');
    router.push('/crawler/tasks');
  } finally {
    loading.value = false;
  }
};

// 返回列表页
const goBack = () => {
  router.push('/crawler/tasks');
};

const exportData = async () => {
  try {
    // 询问用户要导出的数据类型
    const confirmation = await ElMessageBox.confirm(
        '请选择要导出的数据类型',
        '导出数据',
        {
          confirmButtonText: '价格与排名数据',
          cancelButtonText: '评论数据',
          type: 'question',
          closeOnClickModal: false
        }
    );

    // 打印用户选择结果
    console.log("用户确认对话框返回:", confirmation);

    exportLoading.value = true;

    // 根据用户选择确定数据类型
    let dataType;
    if (confirmation === 'confirm') {
      dataType = 'price-rank';
    } else if (confirmation === 'cancel') {
      throw new Error('cancel'); // 用户取消，进入catch分支
    } else {
      // 默认使用当前激活的标签页数据
      dataType = activeTab.value;
      console.warn('无法确定用户选择，使用当前标签页数据:', dataType);
    }

    console.log("导出数据类型:", dataType);

    const response = await exportTaskData(route.params.id, dataType);

    // 处理非标准响应结构
    let blob;
    let status;
    let headers = {};

    // 判断response是否为标准axios响应结构
    if (response && response.data instanceof Blob) {
      blob = response.data;
      status = response.status;
      headers = response.headers;
    } else if (response instanceof Blob) {
      // 非标准响应，直接是Blob
      blob = response;
      status = 200; // 假设成功
      console.warn('接收到非标准响应结构，无法获取状态码和响应头');
    } else {
      // 尝试从非Blob数据创建Blob
      blob = new Blob([response || ''], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      status = response?.status || 500;
      console.error('导出失败：无法处理响应数据', response);
    }

    // 打印响应信息（调试用）
    console.log("导出响应处理结果:", {
      status: status,
      headers: headers,
      blobSize: blob.size,
      blobType: blob.type
    });

    // 验证响应有效性
    if (status !== 200 || blob.size <= 0) {
      // 尝试读取Blob内容作为错误信息
      const reader = new FileReader();
      reader.onload = function() {
        const errorMessage = reader.result.toString().substring(0, 200);
        ElMessage.error(`导出失败: ${errorMessage}`);
      };
      reader.readAsText(blob);
      return;
    }

    // 确保handleBlobDownload函数存在
    if (typeof handleBlobDownload !== 'function') {
      console.error('handleBlobDownload函数未定义，使用备用下载方法');

      // 备用下载逻辑
      const fileName = `${task.value.processName || '未知任务'}_${dataType === 'price-rank' ? '价格与排名' : '评论'}数据_${new Date().toISOString().slice(0, 10)}.xlsx`;
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } else {
      // 使用正常的下载函数
      handleBlobDownload(blob, dataType);
    }

    ElMessage.success('数据导出成功');
  } catch (error) {
    if (error.message === 'cancel') {
      console.log('用户取消导出');
    } else {
      console.error('导出异常:', error);

      // 更详细的错误提示
      if (error.response) {
        ElMessage.error(`导出失败: ${error.response.status} - ${error.response.statusText}`);
      } else if (error.message) {
        ElMessage.error(`导出失败: ${error.message}`);
      } else {
        ElMessage.error('导出失败: 未知错误');
      }
    }
  } finally {
    exportLoading.value = false;
  }
};

// 确保handleBlobDownload函数被正确定义
const handleBlobDownload = (blob, dataType) => {
  try {
    if (!task.value.processName) {
      ElMessage.error('任务名称为空，无法生成文件名');
      return;
    }

    const fileName = `${task.value.processName}_${dataType === 'price-rank' ? '价格与排名' : '评论'}数据_${new Date().toISOString().slice(0, 10)}.xlsx`;

    // 尝试使用file-saver库
    try {
      saveAs(blob, fileName);
      console.log("使用file-saver下载成功");
    } catch (saverError) {
      console.warn("file-saver失败，尝试原生方法:", saverError);

      // 原生下载方法
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      console.log("使用原生方法下载成功");
    }
  } catch (downloadError) {
    console.error("文件下载失败:", downloadError);
    ElMessage.error('文件下载失败，请尝试手动保存');
  }
};

// 工具方法（与任务列表页保持一致）
const getPlatformType = (platform) => {
  const map = {
    'Amazon': 'primary',
    'TEMU': 'success',
    'Shopify': 'warning',
    'Chewy': 'info'
  };
  return map[platform] || 'primary';
};

const getCycleText = (cycle) => {
  const map = {
    'daily': '每天',
    'weekly': '每周',
    'monthly': '每月'
  };
  return map[cycle] || cycle;
};

const getStatusText = (status) => {
  const map = {
    'ACTIVE': '活跃',
    'PAUSED':'暂停',
    'DRAFT': '草稿'
  };
  return map[status] || status;
};

const getStatusType = (status) => {
  const map = {
    'ACTIVE': 'primary',
    'PAUSED': 'warning',
    'DRAFT': 'info'
  };
  return map[status] || 'primary';
};

// 表格行点击事件
const handlePriceRowClick = (row) => {
  // 可以实现点击行查看详情的功能
  console.log('Price row clicked:', row);
};

const handleReviewRowClick = (row) => {
  // 可以实现点击行查看详情的功能
  console.log('Review row clicked:', row);
};

// 标签页切换事件
const handleTabClick = (tab) => {
  console.log('Tab clicked:', tab.name);
};

// 计算属性：拆分 ASIN 列表
const asinList = computed(() => {
  if (task.value.asinList) {
    // 按逗号分隔 ASIN 字符串
    return task.value.asinList.split(',');
  }
  return [];
});

</script>

<style scoped>
.task-detail-container {
  background-color: #fff;
  border-radius: 8px;
  width: 100%;
  height: 100%;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
  display: flex;
  box-sizing: border-box;
  /* 确保flex容器正确扩展 */
  flex-direction: column;
}

.task-info-card {
  margin-bottom: 20px;
  box-shadow: none !important;
  width: 100%;
  box-sizing: border-box;
}

.info-item {
  height: 100%;
}

.review-content-preview {
  color: #409eff;
  cursor: pointer;
}

.el-table {
  width: 100% !important;
  min-width: 100% !important;
}

.el-table__header-wrapper,
.el-table__body-wrapper {
  min-width: 100% !important;
}

/* 调整表格数据字体大小
.el-table {
  font-size: 8px;
}
*/

/* 标签页内容区域铺满 */
.el-tabs__content {
  width: 100%;
  box-sizing: border-box;
}

.info-item {
  height: 100%;
}

.review-content-preview {
  color: #409eff;
  cursor: pointer;
}

/* ASIN 列表样式 */
.asin-item {
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
}

.el-collapse-item__content {
  padding: 10px;
}

/* 导出按钮加载状态样式 */
.btn-group .el-button.is-loading {
  cursor: not-allowed;
}

</style>