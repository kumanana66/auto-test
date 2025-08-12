package com.example.springboot.login.exception;

import com.example.springboot.login.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class AppExceptionHandler {

    // 处理账号锁定异常，包含具体锁定时间
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ResponseDTO<Map<String, Object>>> handleLockedException(LockedException ex) {
        System.out.println("处理账号锁定异常: " + ex.getMessage());

        // 从异常信息中提取分钟数
        String message = ex.getMessage();
        int minutes = 0;
        try {
            minutes = Integer.parseInt(message.replaceAll("\\D", ""));
        } catch (Exception e) {
            minutes = 30; // 默认30分钟
        }

        Map<String, Object> data = new HashMap<>();
        data.put("message", "账号已锁定，请" + minutes + "分钟后再试");
        data.put("lockMinutes", minutes);

        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(ResponseDTO.error("账号已锁定", data));
    }

    // 处理自定义AuthenticationException（用户名不存在等场景）
    @ExceptionHandler(com.example.springboot.login.exception.AuthenticationException.class)
    public ResponseEntity<ResponseDTO<String>> handleCustomAuthenticationException(com.example.springboot.login.exception.AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDTO.error(ex.getMessage())); // 返回401
    }

    // 处理自定义BadCredentialsException（密码错误等场景）
    @ExceptionHandler(com.example.springboot.login.exception.BadCredentialsException.class)
    public ResponseEntity<ResponseDTO<String>> handleCustomBadCredentialsException(com.example.springboot.login.exception.BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDTO.error(ex.getMessage())); // 返回401
    }

    // 处理认证异常，包括密码错误
    @ExceptionHandler({org.springframework.security.authentication.BadCredentialsException.class})
    public ResponseEntity<ResponseDTO<Map<String, Object>>> handleBadCredentialsException(BadCredentialsException ex) {
        System.out.println("处理认证异常: " + ex.getMessage());

        Map<String, Object> data = new HashMap<>();
        String message = ex.getMessage();

        // 尝试从消息中提取剩余重试次数
        int remainingAttempts = -1;
        try {
            String attemptsPattern = "还可重试(\\d+)次";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(attemptsPattern);
            java.util.regex.Matcher m = p.matcher(message);
            if (m.find()) {
                remainingAttempts = Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            // 提取失败，使用默认消息
        }

        data.put("message", message);
        data.put("remainingAttempts", remainingAttempts);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDTO.error("认证失败", data));
    }

    // 处理用户已存在异常
    @ExceptionHandler({UserAlreadyExistsException.class, UsernameExistsException.class})
    public ResponseEntity<ResponseDTO<String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseDTO.error(ex.getMessage()));
    }

    // 处理参数异常
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        // 收集所有字段的错误信息
        for (FieldError error : bindingResult.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        // 构建响应消息：若只有一个错误，直接显示具体信息；否则显示"参数校验失败"
        String message = errors.size() == 1
                ? errors.values().iterator().next()  // 单个错误时，取具体信息
                : "参数校验失败";

        // 返回400状态码 + 错误编码（可自定义，如"VALIDATION_ERROR"）+ 详细信息
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.error(message, errors));
    }

    // 全局兜底异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<String>> handleGlobalException(Exception ex) {
        ex.printStackTrace(); // 日志记录
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error("系统异常，请稍后重试"));
    }

}