CREATE DATABASE IF NOT EXISTS spring_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE login_system;

CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(20) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_login TIMESTAMP COMMENT '最后登录时间',
    is_locked BOOLEAN DEFAULT FALSE COMMENT '账号是否锁定',
    failed_attempts INT DEFAULT 0 COMMENT '失败尝试次数',
    lock_time TIMESTAMP COMMENT '锁定时间',
    avatar  VARCHAR(255) COMMENT '头像',
    email  VARCHAR(30) COMMENT '邮箱',
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
