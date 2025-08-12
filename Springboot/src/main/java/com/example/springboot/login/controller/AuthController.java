package com.example.springboot.login.controller;

import com.example.springboot.login.dto.*;
import com.example.springboot.login.entity.User;
import com.example.springboot.login.exception.UserAlreadyExistsException;
import com.example.springboot.login.repository.UserRepository;
import com.example.springboot.login.service.AuthService;
import com.example.springboot.login.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.login.dto.ResponseDTO;
import com.example.springboot.login.dto.EmailBindRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthService authService,
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authService = authService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: username={}, email={}", request.getUsername(), request.getEmail());
        try {
            // 1. 优先校验用户名、密码、邮箱格式等基础信息（新增逻辑）
            authService.validateRegistrationParams(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail()
            );

            // 2. 最后校验验证码（原逻辑后移）
            if (!authService.verifyCode(request.getEmail(), request.getVerifyCode())) {
                log.warn("验证码验证失败: email={}", request.getEmail());
                return ResponseEntity.badRequest()
                        .body(ResponseDTO.error("验证码错误或已过期"));
            }

            // 3. 执行注册逻辑（保存用户）
            authService.register(request.getUsername(), request.getPassword(), request.getEmail());

            log.info("用户注册成功: username={}", request.getUsername());
            return ResponseEntity.ok(new ResponseDTO<>(true, "注册成功", null));
        } catch (UserAlreadyExistsException e) {
            log.warn("用户名已存在: username={}", request.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(ResponseDTO.error("用户名已被注册"));
        } catch (IllegalArgumentException e) {
            log.warn("注册参数异常: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ResponseDTO.error(e.getMessage()));
        } catch (Exception e) {
            log.error("注册处理发生未预期异常", e);
            return ResponseEntity.status(500)
                    .body(ResponseDTO.error("注册失败，请稍后重试"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiresIn(7200L); // 2小时，单位秒
        return ResponseEntity.ok(new ResponseDTO<>(true, "登录成功", response));
    }

    @GetMapping("/check-username")
    public ResponseEntity<UsernameCheckResponse> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.isUsernameExists(username);
        return ResponseEntity.ok(
                UsernameCheckResponse.builder()
                        .exists(exists)
                        .message(exists ? "用户名已被注册" : "用户名可用")
                        .build()
        );
    }

    @GetMapping("/userinfo")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @AuthenticationPrincipal UserDetails userDetails) {

        // 从Security上下文中获取当前登录用户
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("avatar", user.getAvatar());
        result.put("email", user.getEmail());

        return ResponseEntity.ok(result);
    }

    @PutMapping("/userinfo")
    public ResponseEntity<ResponseDTO<String>> updateUserInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> updateData) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ResponseDTO.error("未认证"));
        }
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查是否需要绑定新邮箱
        if (updateData.containsKey("securityEmail") && updateData.containsKey("verificationCode")) {
            String newEmail = updateData.get("securityEmail").toString();
            String verificationCode = updateData.get("verificationCode").toString();

            // 验证邮箱验证码
            if (!authService.verifyCode(newEmail, verificationCode)) {
                return ResponseEntity.badRequest()
                        .body(ResponseDTO.error("验证码错误或已过期"));
            }

            // 检查邮箱是否已被其他用户绑定
            if (userRepository.findByEmail(newEmail).isPresent() &&
                    !user.getEmail().equals(newEmail)) {
                return ResponseEntity.badRequest()
                        .body(ResponseDTO.error("该邮箱已被其他用户绑定"));
            }

            // 绑定新邮箱
            user.setEmail(newEmail);
        }

        // 更新密码
        if (updateData.containsKey("oldPassword") && updateData.containsKey("newPassword")) {
            String oldPassword = updateData.get("oldPassword").toString();
            String newPassword = updateData.get("newPassword").toString();

            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new IllegalArgumentException("当前密码不正确");
            }

            // 验证新密码强度
            if (!authService.isPasswordStrongEnough(newPassword)) {
                throw new IllegalArgumentException("新密码强度不足，需至少包含大小写字母和数字中的两类");
            }

            // 验证新密码不与旧密码相同
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                throw new IllegalArgumentException("新密码不能与当前密码相同");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(user);

        return ResponseEntity.ok(ResponseDTO.success("个人信息更新成功"));
    }


    @PostMapping("/upload/avatar")
    @Transactional
    public ResponseDTO<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            String avatarUrl = authService.uploadAvatar(username, file);
            return ResponseDTO.success(avatarUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDTO.error(e.getMessage());
        }
    }

    @PostMapping("/validate-password")
    public ResponseEntity<ResponseDTO<Boolean>> validatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> payload) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ResponseDTO.error("未认证"));
        }
        String username = userDetails.getUsername();
        String password = payload.get("password");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("用户不存在: username={}", username);
                    return new RuntimeException("用户不存在");
                });


        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (matches) {
            return ResponseEntity.ok(ResponseDTO.success(true));  // 只有布尔返回
        } else {
            return ResponseEntity.ok(ResponseDTO.success("密码不正确", false)); // 先传消息，再传false
        }
    }

    // 发送邮箱验证码
    @PostMapping("/send-verify-code")
    public ResponseEntity<ResponseDTO<String>> sendVerifyCode(@RequestParam String email) {
        log.info("发送邮箱验证码请求: email={}", email);
        try {
            authService.sendVerificationCode(email);
            return ResponseEntity.ok(ResponseDTO.success("验证码已发送至您的邮箱"));
        } catch (Exception e) {
            log.error("发送验证码失败: email={}", email, e);
            return ResponseEntity.status(500)
                    .body(ResponseDTO.error("发送验证码失败，请稍后重试"));
        }
    }

    @PostMapping("/verify-email-code")
    public ResponseEntity<ResponseDTO<Boolean>> verifyEmailCode(@RequestBody Map<String, String> data) {
        try {
            String email = data.get("email");
            String code = data.get("code");

            boolean isValid = authService.verifyCode(email, code);
            if (isValid) {
                return ResponseEntity.ok(ResponseDTO.success("验证成功", true));
            } else {
                // 验证码错误时返回400状态码
                return ResponseEntity.badRequest()
                        .body(ResponseDTO.error("验证码错误或已过期", false));
            }
        } catch (Exception e) {
            // 记录异常日志
            return ResponseEntity.status(500)
                    .body(ResponseDTO.error("服务器错误，请稍后重试", false));
        }
    }

    @PostMapping("/bind-email")
    public ResponseEntity<ResponseDTO<String>> bindEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody EmailBindRequest request) {

        log.info("绑定邮箱请求: username={}, email={}", userDetails.getUsername(), request.getEmail());

        try {
            // 验证邮箱验证码
            if (!authService.verifyCode(request.getEmail(), request.getVerificationCode())) {
                log.warn("验证码验证失败: email={}", request.getEmail());
                return ResponseEntity.badRequest()
                        .body(ResponseDTO.error("验证码错误或已过期"));
            }

            // 绑定邮箱
            authService.bindEmail(userDetails.getUsername(), request.getEmail());

            return ResponseEntity.ok(ResponseDTO.success("邮箱绑定成功"));
        } catch (Exception e) {
            log.error("绑定邮箱失败", e);
            return ResponseEntity.status(500)
                    .body(ResponseDTO.error("邮箱绑定失败，请稍后重试"));
        }
    }

}
