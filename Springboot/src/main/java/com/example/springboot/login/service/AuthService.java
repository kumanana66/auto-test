package com.example.springboot.login.service;

import com.example.springboot.login.entity.User;
import com.example.springboot.login.entity.VerificationCode;
import com.example.springboot.login.exception.AuthenticationException;
import com.example.springboot.login.exception.UserAlreadyExistsException;
import com.example.springboot.login.repository.UserRepository;
import com.example.springboot.login.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.springboot.login.security.JwtTokenProvider;
import com.example.springboot.login.service.UserService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    // 头像存储路径（可从配置文件中获取）
    private final String avatarStoragePath = "src/main/resources/static/avatars/";
    // 允许的文件类型
    private final String[] allowedImageTypes = {"image/jpg", "image/png"};
    // 最大文件大小（2MB）
    private final long maxFileSize = 2 * 1024 * 1024;

    @Value("${avatar.upload.path}")
    private String avatarUploadPath;

    @Value("${avatar.access.url-prefix}")
    private String avatarAccessUrlPrefix;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;

        // 确保存储目录存在
        try {
            Files.createDirectories(Paths.get(avatarStoragePath));
        } catch (IOException e) {
            throw new RuntimeException("无法创建头像存储目录", e);
        }
    }

    // 注册参数基础校验（用户名、密码、邮箱格式）
    public void validateRegistrationParams(String username, String password, String email) {
        // 校验用户名是否存在
        if (userService.isUsernameExists(username)) {
            throw new UserAlreadyExistsException("用户名已被注册");
        }

        // 校验密码强度
        if (!isPasswordStrongEnough(password)) {
            throw new IllegalArgumentException("密码强度不足，需至少包含大小写字母和数字中的两类");
        }

        // 校验弱密码
        if (isWeakPassword(password)) {
            throw new IllegalArgumentException("密码为常见弱密码，请更换");
        }

        // 新增：校验邮箱格式
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("请输入正确的邮箱格式");
        }
    }

    public void register(String username, String password, String email) {
        if (userService.isUsernameExists(username)) {
            throw new UserAlreadyExistsException("用户名已被注册");
        }
        // 简单验证密码强度
        if (!isPasswordStrongEnough(password)) {
            throw new IllegalArgumentException("密码强度不足，需至少包含大小写字母和数字中的两类");
        }

        // 常见弱密码检查
        if (isWeakPassword(password)) {
            throw new IllegalArgumentException("密码为常见弱密码，请更换");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setCreateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    public String login(String username, String password) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户名或密码错误"));

        // 检查账号是否被锁定
        if (user.isLocked()) {
            LocalDateTime lockTime = user.getLockTime();
            if (lockTime != null && lockTime.isAfter(LocalDateTime.now())) {
                long minutesLeft = java.time.Duration.between(LocalDateTime.now(), lockTime)
                        .toMinutes();
                // 抛出Spring Security原生LockedException，确保被正确处理
                throw new LockedException("账号已锁定，请" + minutesLeft + "分钟后再试");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            user.setLastLogin(LocalDateTime.now())
                    .setFailedAttempts(0)   // 登录成功，重置失败次数和锁定状态
                    .setLocked(false);
            userRepository.save(user);

            return jwtTokenProvider.generateToken(authentication);
        } catch (org.springframework.security.core.AuthenticationException e) {
            // 密码错误，更新失败次数并计算剩余尝试次数
            int failedAttempts = user.getFailedAttempts() + 1;
            int remainingAttempts = 5 - failedAttempts;
            String errorMessage;
            if (failedAttempts >= 5) {
                user.setLocked(true)
                        .setLockTime(LocalDateTime.now().plusMinutes(30));
                errorMessage = "账号已锁定，请30分钟后再试";
            }else {
                // 未达到最大次数，提示剩余尝试次数
                errorMessage = "密码错误，还可重试" + remainingAttempts + "次";
            }

            user.setFailedAttempts(failedAttempts);
            userRepository.save(user);

            try {
                // 使用事务确保更新操作成功
                userRepository.save(user);
            } catch (Exception dbEx) {
                throw new AuthenticationException("用户名或密码错误，请稍后再试");
            }
            // 抛出原始异常，让异常处理器处理
            throw new BadCredentialsException(errorMessage);
        }
    }

    public  boolean isPasswordStrongEnough(String password) {
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            else if (Character.isLowerCase(c)) hasLowerCase = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        int count = 0;
        if (hasUpperCase) count++;
        if (hasLowerCase) count++;
        if (hasDigit) count++;
        return count >= 2;
    }

    private boolean isWeakPassword(String password) {
        String[] weakPasswords = {"123456", "password", "12345678", "qwerty", "12345"};
        for (String weak : weakPasswords) {
            if (password.equals(weak)) {
                return true;
            }
        }
        return false;
    }

    //头像上传
    public String uploadAvatar(String username, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过2MB");
        }

        String contentType = file.getContentType();
        if (!Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/x-png").contains(contentType)) {
            throw new IllegalArgumentException("仅支持JPG、PNG格式的图片");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String filename = username + "_" + System.currentTimeMillis() + extension;

        File dir = new File(avatarUploadPath);
        if (!dir.exists()) dir.mkdirs();
        File dest = new File(avatarUploadPath, filename);
        file.transferTo(dest);

        // 这里返回配置里的prefix + 文件名
        String avatarUrl = avatarAccessUrlPrefix + filename;

        // 保存到用户表
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户不存在"));
        user.setAvatar(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    // 发送邮箱验证码
    public void sendVerificationCode(String email) {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 计算过期时间（5分钟后）
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);

        // 保存验证码到数据库
        VerificationCode codeEntity = new VerificationCode();
        codeEntity.setEmail(email);
        codeEntity.setCode(code);
        codeEntity.setExpireTime(expireTime);
        verificationCodeRepository.save(codeEntity);

        // 发送邮件
        // emailService.sendVerificationCode(email, code);
    }

    // 验证邮箱验证码
    public boolean verifyCode(String email, String code) {
        try {
            LocalDateTime now = LocalDateTime.now();
            // 先删除所有过期的验证码
            verificationCodeRepository.deleteExpiredCodes(now);

            // 查询最新的验证码记录
            List<VerificationCode> codes = verificationCodeRepository
                    .findByEmailAndExpireTimeAfterOrderByCreateTimeDesc(email, now);

            for (VerificationCode c : codes) {
                if (c.getCode().equals(code)) return true;
            }

            return false;
        } catch (Exception e) {
            // 捕获所有异常并记录日志
            return false;
        }
    }

    //绑定邮箱
    public void bindEmail(String username, String email) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户不存在"));

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        // 检查邮箱是否已被其他用户使用
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("该邮箱已被其他用户绑定");
        }

        // 更新用户邮箱
        user.setEmail(email);
        userRepository.save(user);
    }

    // 修改密码
    public void updatePassword(String username, String oldPassword, String newPassword, String email) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }

        // 验证新密码强度
        if (!isPasswordStrongEnough(newPassword)) {
            throw new IllegalArgumentException("新密码强度不足，需至少包含大小写字母和数字中的两类");
        }

        // 验证新密码不与旧密码相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("新密码不能与当前密码相同");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 验证密码
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}