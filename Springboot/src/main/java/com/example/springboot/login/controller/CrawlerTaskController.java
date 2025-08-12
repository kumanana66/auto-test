package com.example.springboot.login.controller;

import com.example.springboot.login.dto.AsinPriceRankDTO;
import com.example.springboot.login.dto.AsinReviewDTO;
import com.example.springboot.login.dto.CrawlerTaskDTO;
import com.example.springboot.login.dto.ResponseDTO;
import com.example.springboot.login.entity.AsinPriceRank;
import com.example.springboot.login.entity.AsinReview;
import com.example.springboot.login.entity.CrawlerTask;
import com.example.springboot.login.service.CrawlerTaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/crawler/tasks")
public class CrawlerTaskController {

    private final CrawlerTaskService crawlerTaskService;
    private Map<Long, String> analyzeStatusMap = new ConcurrentHashMap<>(); // 线程安全的Map

    @Autowired
    public CrawlerTaskController(CrawlerTaskService crawlerTaskService) {
        this.crawlerTaskService = crawlerTaskService;
    }

    // 创建新任务
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<CrawlerTaskDTO>> createTask(@Valid @RequestBody CrawlerTaskDTO taskDTO) {
        CrawlerTask task = taskDTO.toEntity();
        CrawlerTask createdTask = crawlerTaskService.createTask(task);
        CrawlerTaskDTO createdTaskDTO = CrawlerTaskDTO.fromEntity(createdTask);

        return ResponseEntity.ok(
                ResponseDTO.success("任务创建成功", createdTaskDTO)
        );
    }

    // 保存为草稿
    @PostMapping("/draft")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<CrawlerTaskDTO>> saveAsDraft(@RequestBody CrawlerTaskDTO taskDTO) {
        CrawlerTask task = taskDTO.toEntity();
        CrawlerTask draftTask = crawlerTaskService.saveAsDraft(task);
        CrawlerTaskDTO draftTaskDTO = CrawlerTaskDTO.fromEntity(draftTask);

        return ResponseEntity.ok(
                ResponseDTO.success("草稿保存成功", draftTaskDTO)
        );
    }

    // 获取任务列表
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<Page<CrawlerTaskDTO>>> getUserTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String timeCycle,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<CrawlerTask> taskPage = crawlerTaskService.getUserTasksWithFilters(
                status, timeCycle, platform, keyword, pageable);
        Page<CrawlerTaskDTO> taskDTOPage = taskPage.map(CrawlerTaskDTO::fromEntity);

        return ResponseEntity.ok(
                ResponseDTO.success("任务列表获取成功", taskDTOPage)
        );
    }

    // 获取单个任务
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<CrawlerTaskDTO>> getTaskById(@PathVariable Long id) {
        CrawlerTask task = crawlerTaskService.getTaskById(id);
        CrawlerTaskDTO taskDTO = CrawlerTaskDTO.fromEntity(task);

        return ResponseEntity.ok(
                ResponseDTO.success("任务获取成功", taskDTO)
        );
    }

    // 更新任务
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<ResponseDTO<CrawlerTaskDTO>> updateTask(
//            @PathVariable Long id, @RequestBody CrawlerTaskDTO taskDetailsDTO) {
//
//        CrawlerTask taskDetails = taskDetailsDTO.toEntity();
//        CrawlerTask updatedTask = crawlerTaskService.updateTask(id, taskDetails);
//        CrawlerTaskDTO updatedTaskDTO = CrawlerTaskDTO.fromEntity(updatedTask);
//
//        return ResponseEntity.ok(
//                ResponseDTO.success("任务更新成功", updatedTaskDTO)
//        );
//    }

    // 删除任务
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<String>> deleteTask(@PathVariable Long id) {
        crawlerTaskService.deleteTask(id);
        return ResponseEntity.ok(
                ResponseDTO.success("任务删除成功")
        );
    }

    // 暂停任务（status 设为 PAUSED）
    @PostMapping("/{id}/pause")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<String>> pauseTask(@PathVariable Long id) {
        CrawlerTask task = crawlerTaskService.getTaskById(id);
        task.setStatus("PAUSED");
        crawlerTaskService.updateTask(id, task);
        return ResponseEntity.ok(
                ResponseDTO.success("任务已暂停")
        );
    }

    // 执行任务（status 设为 ACTIVE）
    @PostMapping("/{id}/run")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<String>> runTask(@PathVariable Long id) {
        CrawlerTask task = crawlerTaskService.getTaskById(id);
        task.setStatus("ACTIVE");
        crawlerTaskService.updateTask(id, task);
        return ResponseEntity.ok(
                ResponseDTO.success("任务已恢复执行")
        );
    }

    // 获取任务价格数据
    @GetMapping("/{id}/price-ranks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<List<AsinPriceRankDTO>>> getTaskPriceRanks(@PathVariable Long id) {
        try {
            List<AsinPriceRank> priceRanks = crawlerTaskService.getAsinPriceRanksByTaskId(id);
            List<AsinPriceRankDTO> priceRankDTOs = priceRanks.stream()
                    .map(AsinPriceRankDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    ResponseDTO.success("价格数据获取成功", priceRankDTOs)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(ResponseDTO.error("获取价格数据失败：" + e.getMessage()));
        }
    }

    // 获取任务评论数据
    @GetMapping("/{id}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<List<AsinReviewDTO>>> getTaskReviews(@PathVariable Long id) {
        List<AsinReview> reviews = crawlerTaskService.getAsinReviewsByTaskId(id);
        List<AsinReviewDTO> reviewDTOs = reviews.stream()
                .map(AsinReviewDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ResponseDTO.success("评论数据获取成功", reviewDTOs)
        );
    }

    // 导出任务数据
    @GetMapping("/{id}/export")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InputStreamResource> exportTaskData(
            @PathVariable Long id,
            @RequestParam("type") String dataType) throws IOException {

        try {
            // 生成文件名（包含任务名称和时间戳）
            CrawlerTask task = crawlerTaskService.getTaskById(id);
            String fileName = task.getProcessName() + "_" +
                    (dataType.equals("price-rank") ? "价格与排名" : "评论") +
                    "数据_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xlsx";

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(dataType.equals("price-rank") ? "价格与排名数据" : "评论数据");

            // 创建表头
            Row headerRow = sheet.createRow(0);

            if (dataType.equals("price-rank")) {
                // 价格与排名数据导出
                String[] headers = {"ASIN", "品牌", "原价", "LD折扣", "BD折扣", "优惠券", "直降%",
                        "会员价", "会员最终价", "非会员最终价",
                        "大类目", "大类目排名", "小类目", "小类目排名", "爬取时间"};

                // 设置表头样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                // 创建表头单元格
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // 获取数据
                List<AsinPriceRank> priceRanks = crawlerTaskService.getAsinPriceRanksByTaskId(id);

                // 填充数据行
                for (int i = 0; i < priceRanks.size(); i++) {
                    AsinPriceRank rank = priceRanks.get(i);
                    Row dataRow = sheet.createRow(i + 1);

                    // 处理ASIN（字符串类型）
                    setCellValue(dataRow.createCell(0), rank.getAsin());

                    // 处理品牌（字符串类型）
                    setCellValue(dataRow.createCell(1), rank.getBrand());

                    // 处理原价（double类型）
                    setCellValue(dataRow.createCell(2), rank.getOriginalPrice() != null ? rank.getOriginalPrice() : 0.0);

                    // 处理LD折扣（double类型）
                    setCellValue(dataRow.createCell(3), rank.getLdDiscount() != null ? rank.getLdDiscount() : 0.0);

                    // 处理BD折扣（double类型）
                    setCellValue(dataRow.createCell(4), rank.getBdDiscount() != null ? rank.getBdDiscount() : 0.0);

                    // 处理优惠券（字符串类型）
                    setCellValue(dataRow.createCell(5), rank.getCoupon());

                    // 处理直降%（double类型）
                    setCellValue(dataRow.createCell(6), rank.getDirectDiscount() != null ? rank.getDirectDiscount() : 0.0);

                    // 处理会员价（double类型）
                    setCellValue(dataRow.createCell(7), rank.getMemberPrice() != null ? rank.getMemberPrice() : 0.0);

                    // 处理会员最终价（double类型）
                    setCellValue(dataRow.createCell(8), rank.getMemberFinalPrice() != null ? rank.getMemberFinalPrice() : 0.0);

                    // 处理非会员最终价（double类型）
                    setCellValue(dataRow.createCell(9), rank.getNonMemberFinalPrice() != null ? rank.getNonMemberFinalPrice() : 0.0);

                    // 处理大类目（字符串类型）
                    setCellValue(dataRow.createCell(10), rank.getMainCategory());

                    // 处理大类目排名（整数类型）
                    setCellValue(dataRow.createCell(11), rank.getMainCategoryRank() != null ? rank.getMainCategoryRank() : 0);

                    // 处理小类目（字符串类型）
                    setCellValue(dataRow.createCell(12), rank.getSubCategory());

                    // 处理小类目排名（整数类型）
                    setCellValue(dataRow.createCell(13), rank.getSubCategoryRank() != null ? rank.getSubCategoryRank() : 0);

                    // 处理爬取时间（日期类型，支持Date/LocalDateTime/LocalDate）
                    setCellValue(dataRow.createCell(14), rank.getCrawlTime(), workbook);
                }

                // 自动调整列宽
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

            } else {
                // 评论数据导出
                String[] headers = {"ASIN", "品牌", "评论者", "评论日期", "评论内容", "爬取时间"};

                // 设置表头样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                // 创建表头单元格
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // 获取数据
                List<AsinReview> reviews = crawlerTaskService.getAsinReviewsByTaskId(id);

                // 填充数据
                for (int i = 0; i < reviews.size(); i++) {
                    AsinReview review = reviews.get(i);
                    Row dataRow = sheet.createRow(i + 1);

                    // 处理ASIN（字符串类型）
                    setCellValue(dataRow.createCell(0), review.getAsin());

                    // 处理品牌（字符串类型）
                    setCellValue(dataRow.createCell(1), review.getBrand());

                    // 处理评论者（字符串类型）
                    setCellValue(dataRow.createCell(2), review.getReviewerName());

                    // 处理评论日期（日期类型，支持Date/LocalDateTime/LocalDate）
                    setCellValue(dataRow.createCell(3), review.getReviewDate(), workbook);

                    // 处理评论内容（字符串类型）
                    setCellValue(dataRow.createCell(4), review.getReviewContent());

                    // 处理爬取时间（日期类型，支持Date/LocalDateTime/LocalDate）
                    setCellValue(dataRow.createCell(5), review.getCrawlTime(), workbook);
                }

                // 自动调整列宽
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            // 将工作簿转换为输入流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            baos.flush();
            InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20"));
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(new ByteArrayInputStream(
                            ("导出数据失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8))));
        }
    }

    // 处理数据分析，更新Excel和PPT
    @PostMapping("/{id}/analyze")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<String>> analyzeTaskData(@PathVariable Long id) {
        analyzeStatusMap.put(id, "PROCESSING"); // 开始时设为“处理中”

        // 异步执行数据分析逻辑（关键：避免接口阻塞）
        CompletableFuture.runAsync(() -> {
            try {
                // 调用Excel更新和PPT处理逻辑
                CrawlerTask task = crawlerTaskService.getTaskById(id);
                List<AsinPriceRank> priceRanks = crawlerTaskService.getAsinPriceRanksByTaskId(id);
                List<AsinReview> reviews = crawlerTaskService.getAsinReviewsByTaskId(id);

                String excelPath = "D:\\工作记录\\test_python（爬虫）\\ppt_updates\\数据分析报告底表.xlsx";
                updateExcelFile(excelPath, priceRanks, reviews);

                String pptPath = "D:\\工作记录\\test_python（爬虫）\\ppt_updates\\数据分析报告模板.pptx";
                executePythonScript(pptPath, excelPath);

                analyzeStatusMap.put(id, "COMPLETED"); // 成功后设为“完成”
            } catch (Exception e) {
                analyzeStatusMap.put(id, "FAILED"); // 失败设为“失败”
                e.printStackTrace();
            }
        });

        return ResponseEntity.ok(ResponseDTO.success("数据分析已启动"));
    }

    // 下载分析后的PPT
    @GetMapping("/{id}/analyze/ppt")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InputStreamResource> downloadAnalyzedPPT(@PathVariable Long id) throws IOException {
        try {
            String pptPath = "D:\\工作记录\\test_python（爬虫）\\ppt_updates\\数据分析报告模板.pptx";

            // 检查文件是否存在
            File pptFile = new File(pptPath);
            if (!pptFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(
                                "PPT文件不存在".getBytes(StandardCharsets.UTF_8))));
            }

            // 读取文件内容
            InputStream inputStream = new FileInputStream(pptFile);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            String fileName = "数据分析报告_" + id + "_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pptx";
            headers.add("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20"));
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(new ByteArrayInputStream(
                            ("下载PPT失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8))));
        }
    }

    // 查询数据分析状态
    @GetMapping("/{id}/analyze/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<String>> getAnalyzeStatus(@PathVariable Long id) {
        // 实际项目中应使用Redis或数据库存储状态（如"PROCESSING"、"COMPLETED"、"FAILED"）
        // 这里简化处理，假设用内存Map存储（生产环境需替换为分布式存储）
        String status = analyzeStatusMap.getOrDefault(id, "PROCESSING"); // 需在Controller中定义Map作为临时存储
        return ResponseEntity.ok(ResponseDTO.success("查询状态成功", status));
    }

    // 智能设置单元格值（处理不同数据类型，支持null值）
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }

        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    // 处理日期类型并设置日期格式（支持Date/LocalDateTime/LocalDate）
    private void setCellValue(Cell cell, Object dateObj, Workbook workbook) {
        if (dateObj == null) {
            cell.setCellValue("");
            return;
        }

        Date date;
        if (dateObj instanceof LocalDateTime) {
            // LocalDateTime转换为Date
            LocalDateTime localDateTime = (LocalDateTime) dateObj;
            date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } else if (dateObj instanceof LocalDate) {
            // LocalDate转换为Date
            LocalDate localDate = (LocalDate) dateObj;
            date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if (dateObj instanceof Date) {
            // 直接使用Date
            date = (Date) dateObj;
        } else {
            // 无法识别的日期类型，转为字符串
            cell.setCellValue(dateObj.toString());
            return;
        }

        // 设置日期值和格式
        cell.setCellValue(date);
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
        cell.setCellStyle(dateStyle);
    }

    // 更新Excel文件，先清除原有数据再写入新数据
    private void updateExcelFile(String filePath, List<AsinPriceRank> priceRanks, List<AsinReview> reviews) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Excel文件不存在: " + filePath);
        }

        // 读取Excel文件
        Workbook workbook = WorkbookFactory.create(new FileInputStream(file));

        // 处理"价格与排名数据"sheet
        Sheet priceSheet = workbook.getSheet("价格与排名数据");
        if (priceSheet == null) {
            priceSheet = workbook.createSheet("价格与排名数据");
        } else {
            // 清除原有数据（保留表头）
            int lastRowNum = priceSheet.getLastRowNum();
            for (int i = priceSheet.getFirstRowNum() + 1; i <= lastRowNum; i++) {
                Row row = priceSheet.getRow(i);
                if (row != null) {
                    priceSheet.removeRow(row);
                }
            }
        }

        // 写入价格与排名数据的表头
        String[] priceHeaders = {"ASIN", "品牌", "原价", "LD折扣", "BD折扣", "优惠券", "直降%",
                "会员价", "会员最终价", "非会员最终价", "大类目", "大类目排名", "小类目", "小类目排名", "爬取时间"};
        Row priceHeaderRow = priceSheet.createRow(0);
        for (int i = 0; i < priceHeaders.length; i++) {
            Cell cell = priceHeaderRow.createCell(i);
            cell.setCellValue(priceHeaders[i]);
        }

        // 写入价格与排名数据
        for (int i = 0; i < priceRanks.size(); i++) {
            AsinPriceRank rank = priceRanks.get(i);
            Row dataRow = priceSheet.createRow(i + 1);

            setCellValue(dataRow.createCell(0), rank.getAsin());
            setCellValue(dataRow.createCell(1), rank.getBrand());
            setCellValue(dataRow.createCell(2), rank.getOriginalPrice());
            setCellValue(dataRow.createCell(3), rank.getLdDiscount());
            setCellValue(dataRow.createCell(4), rank.getBdDiscount());
            setCellValue(dataRow.createCell(5), rank.getCoupon());
            setCellValue(dataRow.createCell(6), rank.getDirectDiscount());
            setCellValue(dataRow.createCell(7), rank.getMemberPrice());
            setCellValue(dataRow.createCell(8), rank.getMemberFinalPrice());
            setCellValue(dataRow.createCell(9), rank.getNonMemberFinalPrice());
            setCellValue(dataRow.createCell(10), rank.getMainCategory());
            setCellValue(dataRow.createCell(11), rank.getMainCategoryRank());
            setCellValue(dataRow.createCell(12), rank.getSubCategory());
            setCellValue(dataRow.createCell(13), rank.getSubCategoryRank());
            setCellValue(dataRow.createCell(14), rank.getCrawlTime(), workbook);
        }

        // 处理"评论数据"sheet
        Sheet reviewSheet = workbook.getSheet("评论数据");
        if (reviewSheet == null) {
            reviewSheet = workbook.createSheet("评论数据");
        } else {
            // 清除原有数据（保留表头）
            int lastRowNum = reviewSheet.getLastRowNum();
            for (int i = reviewSheet.getFirstRowNum() + 1; i <= lastRowNum; i++) {
                Row row = reviewSheet.getRow(i);
                if (row != null) {
                    reviewSheet.removeRow(row);
                }
            }
        }

        // 写入评论数据的表头
        String[] reviewHeaders = {"ASIN", "品牌", "评论者", "评论日期", "评论内容", "爬取时间"};
        Row reviewHeaderRow = reviewSheet.createRow(0);
        for (int i = 0; i < reviewHeaders.length; i++) {
            Cell cell = reviewHeaderRow.createCell(i);
            cell.setCellValue(reviewHeaders[i]);
        }

        // 写入评论数据
        for (int i = 0; i < reviews.size(); i++) {
            AsinReview review = reviews.get(i);
            Row dataRow = reviewSheet.createRow(i + 1);

            setCellValue(dataRow.createCell(0), review.getAsin());
            setCellValue(dataRow.createCell(1), review.getBrand());
            setCellValue(dataRow.createCell(2), review.getReviewerName());
            setCellValue(dataRow.createCell(3), review.getReviewDate(), workbook);
            setCellValue(dataRow.createCell(4), review.getReviewContent());
            setCellValue(dataRow.createCell(5), review.getCrawlTime(), workbook);
        }

        // 自动调整列宽
        for (int i = 0; i < priceHeaders.length; i++) {
            priceSheet.autoSizeColumn(i);
        }
        for (int i = 0; i < reviewHeaders.length; i++) {
            reviewSheet.autoSizeColumn(i);
        }

        // 保存文件
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        // 关闭工作簿
        workbook.close();
    }

    // 执行Python脚本更新PPT
    private void executePythonScript(String pptPath, String excelPath) throws IOException, InterruptedException {
        // 指定Python脚本文件路径
        String pythonScriptPath = "D:\\工作记录\\test_python（爬虫）\\ppt_updates\\update_ppt.py";

        // 检查脚本文件是否存在
        File scriptFile = new File(pythonScriptPath);
        if (!scriptFile.exists()) {
            throw new FileNotFoundException("Python脚本文件不存在: " + pythonScriptPath);
        }

        // 执行Python脚本，传递PPT和Excel路径作为参数
        ProcessBuilder pb = new ProcessBuilder(
                "python",
                pythonScriptPath,
                pptPath,  // 第一个参数：PPT文件路径
                excelPath // 第二个参数：Excel文件路径
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 读取输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Python输出: " + line);
            }
        }

        // 等待脚本执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python脚本执行失败，退出码: " + exitCode);
        }
    }
}