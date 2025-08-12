package com.example.springboot.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @NotBlank(message = "邮箱不能为空")
    private String securityEmail;

    @NotBlank(message = "验证码不能为空")
    private String verificationCode;
}

