import request from './request'

// 创建爬虫任务
export function postCrawlerTask(data) {
    return request({
        url: '/api/crawler/tasks',
        method: 'post',
        data
    })
}

// 保存草稿
export function saveCrawlerTaskDraft(data) {
    return request({
        url: '/api/crawler/tasks/draft',
        method: 'post',
        data
    })
}

// 获取任务列表
// 获取任务列表（新增分页参数）
export function getCrawlerTasks(params) {
    return request({
        url: '/api/crawler/tasks',
        method: 'get',
        params
    })
}

// 获取单个任务详情
export function getCrawlerTask(id) {
    return request({
        url: `/api/crawler/tasks/${id}`,
        method: 'get'
    })
}

// 获取任务价格&排名数据
export function getTaskPriceRanks(id) {
    return request({
        url: `/api/crawler/tasks/${id}/price-ranks`,
        method: 'get'
    });
}

// 获取任务评论数据
export function getTaskReviews(id) {
    return request({
        url: `/api/crawler/tasks/${id}/reviews`,
        method: 'get'
    });
}

// 删除任务
export function deleteCrawlerTask(id) {
    return request({
        url: `/api/crawler/tasks/${id}`,
        method: 'delete'
    });
}

// 暂停任务
export function pauseCrawlerTask(id) {
    return request({
        url: `/api/crawler/tasks/${id}/pause`,
        method: 'post'
    });
}

// 执行任务
export function runCrawlerTask(id) {
    return request({
        url: `/api/crawler/tasks/${id}/run`,
        method: 'post'
    });
}

// 导出任务数据
export function exportTaskData(id, dataType) {
    return request({
        url: `/api/crawler/tasks/${id}/export`,
        method: 'get',
        params: { type: dataType },
        responseType: 'blob' // 指定响应类型为二进制流
    });
}

// 触发任务数据分析
export const analyzeTaskData = (taskId) => {
    return request({
        url: `/api/crawler/tasks/${taskId}/analyze`,
        method: 'post',
        timeout: 1800000
    });
};

// 下载分析后的PPT
export const downloadAnalyzedPPT = (taskId) => {
    return request({
        url: `/api/crawler/tasks/${taskId}/analyze/ppt`,
        method: 'get',
        responseType: 'blob',
        timeout: 1800000
    });
};

// 查询数据分析状态
export const getAnalyzeStatus = (taskId) => {
    return request({
        url: `/api/crawler/tasks/${taskId}/analyze/status`,
        method: 'get',
        timeout: 50000
    });
};