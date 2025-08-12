package com.example.springboot.login.dto;

import com.example.springboot.login.entity.CrawlerTask;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerTaskDTO {

    private Long id;

    @NotBlank(message = "任务名称不能为空")
    private String processName;

    @NotBlank(message = "ASIN列表不能为空")
    private String asinList;

    @NotEmpty(message = "请至少选择一项所需信息")
    private List<String> requiredInfo;

    @NotBlank(message = "请选择平台")
    private String platform;

    @NotBlank(message = "请选择时间周期")
    private String timeCycle;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String status;
    private Long userId;
    private String username; // 用户名称，方便前端显示

    // 从实体类转换为DTO
    public static CrawlerTaskDTO fromEntity(CrawlerTask task) {
        CrawlerTaskDTO dto = new CrawlerTaskDTO();
        dto.setId(task.getId());
        dto.setProcessName(task.getProcessName());
        dto.setAsinList(task.getAsinList());
        dto.setRequiredInfo(task.getRequiredInfo());
        dto.setPlatform(task.getPlatform());
        dto.setTimeCycle(task.getTimeCycle());
        dto.setCreateTime(task.getCreateTime());
        dto.setUpdateTime(task.getUpdateTime());
        dto.setStatus(task.getStatus());

        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getId());
            dto.setUsername(task.getUser().getUsername());
        }

        return dto;
    }

    // 从DTO转换为实体类
    public CrawlerTask toEntity() {
        CrawlerTask task = new CrawlerTask();
        task.setId(this.getId());
        task.setProcessName(this.getProcessName());
        task.setAsinList(this.getAsinList());
        task.setRequiredInfo(this.getRequiredInfo());
        task.setPlatform(this.getPlatform());
        task.setTimeCycle(this.getTimeCycle());
        if (task.getCreateTime() == null) {
            task.setCreateTime(LocalDateTime.now());
        }
        task.setUpdateTime(this.getUpdateTime());
        task.setStatus(this.getStatus());

        return task;
    }

    // 转换实体类列表为DTO列表
    public static List<CrawlerTaskDTO> fromEntityList(List<CrawlerTask> tasks) {
        return tasks.stream()
                .map(CrawlerTaskDTO::fromEntity)
                .collect(Collectors.toList());
    }
}