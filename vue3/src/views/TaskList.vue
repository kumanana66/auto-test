<template>
  <div class="task-list-container">
    <!-- 加载态 -->
    <div v-if="loading" class="loading-container">
      <el-loading :fullscreen="true" :lock="true" text="加载中..." spinner="el-icon-loading"></el-loading>
    </div>

    <div v-else class="task-list-content">
      <!-- 页面标题和操作按钮 -->
      <div class="page-header">
        <h2 class="page-title">爬虫任务列表</h2>
        <div class="action-buttons">
          <el-button type="primary" @click="goToCreateTask">创建任务</el-button>
          <el-button @click="refreshTasks">刷新</el-button>
          <el-button @click="clearFilters">清除筛选</el-button>
        </div>
      </div>

      <!-- 搜索和筛选区域 -->
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="任务状态">
            <el-select v-model="filter.status" placeholder="请选择状态" @change="handleFilterChange">
              <el-option label="全部" value="" />
              <el-option label="活跃" value="ACTIVE" />
              <el-option label="暂停" value="PAUSED" />
              <el-option label="草稿" value="DRAFT" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="执行周期">
            <el-select v-model="filter.timeCycle" placeholder="请选择周期" @change="handleFilterChange">
              <el-option label="全部" value="" />
              <el-option label="每天" value="daily" />
              <el-option label="每周" value="weekly" />
              <el-option label="每月" value="monthly" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="执行平台">
            <el-select v-model="filter.platform" placeholder="请选择平台" @change="handleFilterChange">
              <el-option label="全部" value="" />
              <el-option label="Amazon" value="Amazon" />
              <el-option label="TEMU" value="TEMU" />
              <el-option label="Shopify" value="Shopify" />
              <el-option label="Chewy" value="Chewy" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-input
                v-model="filter.keyword"
                placeholder="搜索任务名称或ASIN"
                suffix-icon="el-icon-search"
                @keyup.enter="filterTasks"
            >
              <template #append>
                <el-button @click="filterTasks">搜索</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 任务表格 -->
      <el-table
          v-loading="tableLoading"
          :data="filteredTasks"
          stripe
          style="width: 100%"
      >
        <el-table-column prop="processName" label="任务名称" min-width="180" />
        <el-table-column prop="asinList" label="ASIN列表" min-width="200">
          <template #default="{ row }">
            <div v-if="row.asinList.length > 30">
              {{ row.asinList.substring(0, 30) }}...
            </div>
            <div v-else>{{ row.asinList }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="requiredInfo" label="所需信息" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="info in row.requiredInfo" :key="info" size="small">
              {{ getInfoText(info) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="platform" label="平台" width="100">
          <template #default="{ row }">
            <el-tag :type="getPlatformType(row.platform)">{{ row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timeCycle" label="执行周期" width="100">
          <template #default="{ row }">
            <el-tag>{{ getCycleText(row.timeCycle) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="{ row }">
            <span>{{ $formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="290">
          <template #default="{ row }">
            <el-button size="small" type="info" @click="analyzeData(row.id)">
              数据分析
            </el-button>
            <el-button size="small" type="primary" @click="goToDetail(row.id)">
              查看
            </el-button>
            <el-button
                size="small"
                :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
                @click="toggleTaskStatus(row)"
                :disabled="row.status === 'DRAFT'"
                style="margin-left: 6px;"
            >
              {{ row.status === 'ACTIVE' ? '暂停' : row.status === 'PAUSED' ? '恢复' : '不可用' }}
            </el-button>
            <el-button
                size="small"
                type="danger"
                style="margin-left: 6px;"
                @click="confirmDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-paginationch
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalCount"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getCrawlerTasks,
  deleteCrawlerTask,
  pauseCrawlerTask,
  runCrawlerTask,
  analyzeTaskData,
  getAnalyzeStatus,
  downloadAnalyzedPPT
} from '../api/crawler';

const router = useRouter();

const loading = ref(true);
const tableLoading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalCount = ref(0);
const tasks = ref([]);
const filter = reactive({
  status: '',
  timeCycle: '',
  platform: '',
  keyword: ''
});

// 初始化
onMounted(async () => {
  await fetchTasks();
});

// 获取任务列表
const fetchTasks = async (page = currentPage.value, size = pageSize.value) => {
  loading.value = true;
  tableLoading.value = true;

  try {
    const response = await getCrawlerTasks({
      page, // 后端通常从0开始分页
      size,
      status: filter.status,
      timeCycle: filter.timeCycle,
      platform: filter.platform,
      keyword: filter.keyword
    });

    if (response && response.success) {
      tasks.value = response.data.content || [];
      totalCount.value = response.data.totalElements || 0;
    } else {
      ElMessage.error(response.message || '获取任务列表失败');
    }
  } catch (error) {
    console.error('获取任务列表异常', error);
    ElMessage.error('获取任务列表失败，请稍后重试');
  } finally {
    loading.value = false;
    tableLoading.value = false;
  }
};

// 处理筛选条件变更
const handleFilterChange = () => {
  currentPage.value = 1; // 重置到第一页
  fetchTasks();
};

// 关键词搜索（保留原有逻辑）
const filterTasks = () => {
  currentPage.value = 1; // 重置到第一页
  fetchTasks();
};

// 刷新任务
const refreshTasks = () => {
  fetchTasks();
};

// 清除筛选条件
const clearFilters = () => {
  filter.status = '';
  filter.timeCycle = '';
  filter.platform = '';
  filter.keyword = '';
  currentPage.value = 1; // 重置到第一页
  fetchTasks();
};

// 处理分页
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchTasks();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchTasks();
};

// 工具方法
const getInfoText = (key) => {
  const map = {
    'price': '价格',
    'seller_rank': '类目排名',
    'review': '评论'
  };
  return map[key] || key;
};

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
    'DRAFT': '草稿',
    'PAUSED': '暂停'
  };
  return map[status] || status;
};

const getStatusType = (status) => {
  const map = {
    'ACTIVE': 'primary',
    'DRAFT': 'info',
    'PAUSED': 'warning'
  };
  return map[status] || 'primary';
};

// 任务操作-暂停/执行
const toggleTaskStatus = async (row) => {
  try {
    if (row.status === 'ACTIVE') {
      const res = await pauseCrawlerTask(row.id);
      if (res && res.success) {
        ElMessage.success('任务已暂停');
        fetchTasks();
      } else {
        ElMessage.error(res.message || '暂停失败');
      }
    } else if (row.status === 'PAUSED') {
      const res = await runCrawlerTask(row.id);
      if (res && res.success) {
        ElMessage.success('任务已恢复执行');
        fetchTasks();
      } else {
        ElMessage.error(res.message || '恢复失败');
      }
    }
  } catch (err) {
    ElMessage.error('操作失败，请稍后重试');
  }
};

// 页面跳转
const goToCreateTask = () => {
  router.push('/crawler/tasks/create');
};

const goToDetail = (id) => {
  router.push(`/crawler/tasks/${id}`);
};

// 删除任务
const confirmDelete = (id) => {
  ElMessageBox.confirm(
      '确认删除该任务吗？此操作不可恢复。',
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
  )
      .then(async () => {
        try {
          const response = await deleteCrawlerTask(id);
          if (response && response.success) {
            ElMessage.success('任务删除成功');
            fetchTasks();
          } else {
            ElMessage.error(response.message || '删除任务失败');
          }
        } catch (error) {
          console.error('删除任务异常', error);
          ElMessage.error('删除任务失败，请稍后重试');
        }
      })
      .catch(() => {
        // 取消删除
      });
};

const analyzeMsg = reactive({
  instance: null,
  show: false
});

// 数据分析按钮点击事件
const analyzeData = async (taskId) => {
  try {
    // 1. 触发后端开始数据分析（不等待完成，仅发送启动请求）
    const startResponse = await analyzeTaskData(taskId);
    if (!startResponse.success) {
      ElMessage.error("启动数据分析失败：" + startResponse.message);
      return;
    }

    analyzeMsg.instance = ElMessage({
      message: "数据分析已启动，正在处理中...",
      type: "info",
      duration: 0,
      showClose: true
    });

    // 2. 轮询查询处理状态（每3秒查一次）
    const checkStatus = async () => {
      try {
        const statusResponse = await getAnalyzeStatus(taskId); // 需新增这个API
        if (statusResponse.success) {
          const status = statusResponse.data;

          if (status === "COMPLETED") {
            // 3. 状态为“完成”，触发PPT下载
            analyzeMsg.instance.close(); // 关闭启动提示
            ElMessage.success("数据分析完成，正在下载PPT...");
            const pptResponse = await downloadAnalyzedPPT(taskId);
            // 处理PPT下载（同之前的逻辑）
            const blob = new Blob([pptResponse.data], {
              type: 'application/vnd.openxmlformats-officedocument.presentationml.presentation'
            });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `数据分析报告_${taskId}_${new Date().toISOString().slice(0, 10)}.pptx`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
            ElMessage.success("PPT下载成功");
          } else if (status === "FAILED") {
            ElMessage.error("数据分析失败，请重试");
            analyzeMsg.instance.close();
          } else {
            // 未完成，继续轮询（3秒后再查）
            setTimeout(checkStatus, 3000);
          }
        } else {
          ElMessage.error("查询状态失败，将重试...");
          setTimeout(checkStatus, 3000);
        }
      } catch (error) {
        console.error("轮询状态异常", error);
        setTimeout(checkStatus, 3000); // 出错也继续轮询
      }
    };

    // 启动第一次轮询
    checkStatus();
  } catch (error) {
    if (error.code === "ECONNABORTED") { // 超时错误
      ElMessage({
        message: "请求超时，但任务可能已启动，将继续等待结果...",
        type: "warning",
        duration: 0
      });
      // 继续轮询
      setTimeout(() => checkStatus(), 3000);
    } else {
      ElMessage.error("发生错误：" + error.message);
    }
  }
};

// 过滤（保留原有计算属性，虽然未实际使用）
const filteredTasks = computed(() => tasks.value);

</script>

<style scoped>
.task-list-container {
  background-color: #fff;
  border-radius: 8px;
  width: 100%;
  height: 100%;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
  display: flex;
  box-sizing: border-box;
}

.task-list-content {
  flex: 1 1 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.el-table {
  flex: 1 1 0;
  min-height: 0;
  overflow: auto;
}

.pagination-container {
  margin-top: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.action-buttons {
  display: flex;
  gap: 10px;
}
.el-row {
  margin-bottom: 30px;
}
</style>