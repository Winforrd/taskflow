CREATE DATABASE taskflow
USE taskflow;

CREATE TABLE sys_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                          username VARCHAR(50) NOT NULL COMMENT '用户名',
                          password VARCHAR(100) NOT NULL COMMENT '密码',
                          nickname VARCHAR(50) NOT NULL COMMENT '昵称',
                          avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
                          email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                          phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1正常',
                          created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                          UNIQUE KEY uk_username (username),
                          UNIQUE KEY uk_email (email)
) COMMENT='用户表';