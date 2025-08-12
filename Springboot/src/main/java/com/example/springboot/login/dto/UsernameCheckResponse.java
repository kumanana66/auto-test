package com.example.springboot.login.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsernameCheckResponse {
    private boolean exists;
    private String message;
}