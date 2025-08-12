package com.example.springboot.login.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private Long expiresIn; // 过期时间(秒)
}