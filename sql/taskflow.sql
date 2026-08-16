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
CREATE TABLE team (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '团队ID',

                      name VARCHAR(100) NOT NULL COMMENT '团队名称',

                      description VARCHAR(500) DEFAULT NULL COMMENT '团队简介',

                      avatar VARCHAR(255) DEFAULT NULL COMMENT '团队头像',

                      owner_id BIGINT NOT NULL COMMENT '团队所有者用户ID',

                      status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0停用',

                      created_by BIGINT NOT NULL COMMENT '创建人',

                      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                      deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                      KEY idx_owner_id (owner_id),

                      KEY idx_created_by (created_by)

) COMMENT='团队表';
CREATE TABLE team_member (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

                             team_id BIGINT NOT NULL COMMENT '团队ID',

                             user_id BIGINT NOT NULL COMMENT '用户ID',

                             role_type VARCHAR(20) NOT NULL COMMENT '团队角色：OWNER/ADMIN/MEMBER/READ_ONLY',

                             member_status TINYINT NOT NULL DEFAULT 1 COMMENT '成员状态：1正常，0禁用',

                             joined_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',

                             created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                             UNIQUE KEY uk_team_user (team_id, user_id),

                             KEY idx_user_id (user_id),

                             KEY idx_team_id (team_id)

) COMMENT='团队成员表';
CREATE TABLE team_invitation (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '邀请记录ID',

                                 team_id BIGINT NOT NULL COMMENT '团队ID',

                                 invitation_code VARCHAR(64) NOT NULL COMMENT '邀请码',

                                 creator_id BIGINT NOT NULL COMMENT '创建人用户ID',

                                 default_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER'
                                     COMMENT '加入后的默认角色：MEMBER/READ_ONLY',

                                 expires_at DATETIME NOT NULL COMMENT '过期时间',

                                 max_uses INT NOT NULL DEFAULT 1 COMMENT '最大使用次数',

                                 used_count INT NOT NULL DEFAULT 0 COMMENT '已使用次数',

                                 status TINYINT NOT NULL DEFAULT 1
                                     COMMENT '状态：1有效，0禁用',

                                 created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,

                                 UNIQUE KEY uk_invitation_code (invitation_code),

                                 KEY idx_team_id (team_id),

                                 KEY idx_creator_id (creator_id),

                                 KEY idx_expires_at (expires_at)

) COMMENT='团队邀请表';

CREATE TABLE project (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',

                         team_id BIGINT NOT NULL COMMENT '所属团队ID',

                         project_code VARCHAR(30) NOT NULL COMMENT '项目编号',

                         name VARCHAR(100) NOT NULL COMMENT '项目名称',

                         description VARCHAR(1000) DEFAULT NULL COMMENT '项目描述',

                         owner_id BIGINT NOT NULL COMMENT '项目负责人用户ID',

                         status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'   COMMENT '项目状态：NOT_STARTED/IN_PROGRESS/COMPLETED/ARCHIVED',

                         start_date DATE DEFAULT NULL COMMENT '开始日期',

                         deadline DATE DEFAULT NULL COMMENT '截止日期',

                         created_by BIGINT NOT NULL COMMENT '创建人用户ID',

                         created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP    ON UPDATE CURRENT_TIMESTAMP,

                         deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                         UNIQUE KEY uk_team_project_code (team_id,  project_code),

                         KEY idx_project_team_id (team_id),

                         KEY idx_project_owner_id (owner_id),

                         KEY idx_project_status (status)

) COMMENT='项目表';

CREATE TABLE project_member (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

                                project_id BIGINT NOT NULL COMMENT '项目ID',

                                user_id BIGINT NOT NULL COMMENT '用户ID',

                                role_type VARCHAR(20) NOT NULL
                                    COMMENT '项目角色：MANAGER/MEMBER/READ_ONLY',

                                joined_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMPCOMMENT '加入项目时间',

                                created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMPON UPDATE CURRENT_TIMESTAMP,

                                UNIQUE KEY uk_project_user (project_id,user_id),

                                KEY idx_project_member_user_id (user_id),

                                KEY idx_project_member_project_id (project_id)
) COMMENT='项目成员表';