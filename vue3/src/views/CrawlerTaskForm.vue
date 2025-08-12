<template>
  <section class="process-form">
    <h2 class="form-header">爬虫任务创建表单</h2>
    <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="form-wrapper"
    >
      <!-- 1. 流程名称 -->
      <el-form-item label="流程名称" prop="processName">
        <el-input
            v-model="form.processName"
            placeholder="请输入流程名称"
        />
      </el-form-item>

      <!-- 2. 批量 ASIN -->
      <el-form-item label="ASIN列表" prop="asinList">
        <el-input
            v-model="form.asinList"
            type="textarea"
            placeholder="请输入ASIN（使用、;,或回车分隔）"
            rows="4"
        />
        <div class="tip">提示：可用、;,或回车分隔</div>
      </el-form-item>

      <!-- 3. 所需信息复选框 -->
      <el-form-item label="所需信息" prop="requiredInfo">
        <el-checkbox-group v-model="form.requiredInfo">
          <el-checkbox label="price">价格</el-checkbox>
          <el-checkbox label="seller_rank">类目排名</el-checkbox>
          <el-checkbox label="review">评论</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <!-- 4. 抓取平台单选 -->
      <el-form-item label="平台" prop="platform">
        <el-radio-group v-model="form.platform">
          <el-radio label="Amazon">Amazon</el-radio>
          <el-radio label="TEMU">TEMU</el-radio>
          <el-radio label="Shopify">Shopify</el-radio>
          <el-radio label="Chewy">Chewy</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 5. 时间周期单选 -->
      <el-form-item label="时间周期" prop="timeCycle">
        <el-radio-group v-model="form.timeCycle">
          <el-radio label="daily">每天</el-radio>
          <el-radio label="weekly">每周</el-radio>
          <el-radio label="monthly">每月</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" @click="submitForm">提交</el-button>
        <el-button @click="saveAsDraft">保存为草稿</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { postCrawlerTask, saveCrawlerTaskDraft } from '../api/crawler'; // 引入API请求

const router = useRouter();
const formRef = ref(null);

// 初始化表单数据
const form = reactive({
  processName: '',
  asinList: '',
  requiredInfo: [],
  platform: 'Amazon',
  timeCycle: 'daily'
});

const rules = reactive({
  processName: [
    { required: true, message: '请输入流程名称', trigger: 'blur' }
  ],
  asinList: [
    { required: true, message: '请输入ASIN列表', trigger: 'blur' }
  ],
  requiredInfo: [
    { required: true, type: 'array', min: 1, message: '请至少选择一项所需信息', trigger: 'change' }
  ],
  platform:[
    { required: true, message: '请选择抓取平台', trigger: 'change' }
  ],
  timeCycle: [
    { required: true, message: '请选择时间周期', trigger: 'change' }
  ]
});

const submitForm = async () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 处理 ASIN 分隔
        const asins = form.asinList
            .replace(/[、;,\n]/g, ',') // 替换分隔符为逗号
            .split(',')
            .map(asin => asin.trim())
            .filter(asin => asin);

        // 准备提交数据
        const taskData = {
          processName: form.processName,
          asinList: asins.join(','), // 存储为逗号分隔的字符串
          requiredInfo: form.requiredInfo,
          platform: form.platform,
          timeCycle: form.timeCycle,
          status: 'ACTIVE', // 设置任务状态为活跃
          createTime: new Date().toISOString() // 添加创建时间
        };

        // 调用API保存任务
        const response = await postCrawlerTask(taskData);

        if (response && response.success) {
          ElMessage.success('爬虫任务创建成功');
          // 提交成功后跳转到任务列表页面
          router.push('/crawler/tasks');
        } else {
          ElMessage.error(response.message || '保存任务失败，请稍后重试');
        }
      } catch (error) {
        console.error('保存任务失败', error);
        ElMessage.error('保存任务失败，请稍后重试');
      }
    } else {
      ElMessage.error('请完善必填项');
    }
  });
};

const saveAsDraft = async () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 处理 ASIN 分隔
        const asins = form.asinList
            .replace(/[、;,\n]/g, ',')
            .split(',')
            .map(asin => asin.trim())
            .filter(asin => asin);

        // 准备草稿数据
        const draftData = {
          processName: form.processName,
          asinList: asins.join(','),
          requiredInfo: form.requiredInfo,
          platform: form.platform,
          timeCycle: form.timeCycle,
          status: 'DRAFT', // 草稿状态
          createTime: new Date().toISOString()
        };

        // 调用保存草稿API
        const response = await saveCrawlerTaskDraft(draftData);

        if (response && response.success) {
          ElMessage.info('已保存为草稿');
          // 可以选择跳转到草稿列表或留在当前页面
          router.push('/crawler/drafts');
        } else {
          ElMessage.error(response.message || '保存草稿失败，请稍后重试');
        }
      } catch (error) {
        console.error('保存草稿失败', error);
        ElMessage.error('保存草稿失败，请稍后重试');
      }
    } else {
      ElMessage.error('请完善必填项');
    }
  });
};
</script>

<style scoped>
.process-form {
  background-color: #fff;
  border-radius: 8px;
  width: 100%;
  height: 100%;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
  box-sizing: border-box;
}

.form-header {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.form-wrapper {
  max-width: 100%;
  margin: 0 auto;
}

.tip {
  margin-top: 4px;
  color: #999;
  font-size: 12px;
}
</style>