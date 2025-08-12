package com.example.springboot.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor // Lombok 会自动生成包含所有字段的构造函数
public class PageData<T> {
    private List<T> records;     // 当前页数据
    private int currentPage;     // 当前页码
    private int pageSize;        // 每页大小
    private long total;          // 总记录数
    private int pages;           // 总页数
}