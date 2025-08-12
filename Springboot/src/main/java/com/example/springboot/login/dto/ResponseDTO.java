package com.example.springboot.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ResponseDTO<T> success(T data) {

        return new ResponseDTO<>(true, "操作成功", data);
    }

    public static <T> ResponseDTO<T> success(String message, T data) {

        return new ResponseDTO<>(true, message, data);
    }

    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>(false, message, null);
    }

    // 新增：支持传入数据对象的 error 方法
    public static <T> ResponseDTO<T> error(T data) {

        return new ResponseDTO<>(false, "操作失败", data);
    }

    // 新增：同时包含 message 和 data 的 error 方法
    public static <T> ResponseDTO<T> error(String message, T data) {

        return new ResponseDTO<>(false, message, data);
    }
}