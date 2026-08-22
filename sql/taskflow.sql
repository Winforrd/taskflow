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

CREATE TABLE task (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',

                      project_id BIGINT NOT NULL COMMENT '所属项目ID',

                      title VARCHAR(200) NOT NULL COMMENT '任务标题',

                      description TEXT DEFAULT NULL COMMENT '任务描述',

                      status VARCHAR(30) NOT NULL DEFAULT 'TODO'
                          COMMENT '任务状态：TODO/IN_PROGRESS/IN_REVIEW/DONE/CANCELLED',

                      priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM'
                          COMMENT '优先级：LOW/MEDIUM/HIGH/URGENT',

                      creator_id BIGINT NOT NULL COMMENT '创建人用户ID',

                      start_date DATE DEFAULT NULL COMMENT '开始日期',

                      deadline DATE DEFAULT NULL COMMENT '截止日期',

                      completed_time DATETIME DEFAULT NULL COMMENT '完成时间',

                      version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

                      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP,

                      deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                      KEY idx_task_project_id (project_id),

                      KEY idx_task_creator_id (creator_id),

                      KEY idx_task_status (status),

                      KEY idx_task_priority (priority),

                      KEY idx_task_deadline (deadline)

) COMMENT='任务表';

CREATE TABLE task_assignee (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

                               task_id BIGINT NOT NULL COMMENT '任务ID',

                               user_id BIGINT NOT NULL COMMENT '负责人用户ID',

                               assigned_by BIGINT NOT NULL COMMENT '指派人用户ID',

                               assigned_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               UNIQUE KEY uk_task_user (
                                   task_id,
                                   user_id
                                   ),

                               KEY idx_assignee_task_id (task_id),

                               KEY idx_assignee_user_id (user_id)

) COMMENT='任务负责人表';

CREATE TABLE task_status_record (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '状态记录ID',

                                    task_id BIGINT NOT NULL COMMENT '任务ID',

                                    old_status VARCHAR(30) DEFAULT NULL COMMENT '原状态',

                                    new_status VARCHAR(30) NOT NULL COMMENT '新状态',

                                    operator_id BIGINT NOT NULL COMMENT '操作人',

                                    changed_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    KEY idx_status_record_task_id (task_id)

) COMMENT='任务状态流转记录表';

CREATE TABLE task_activity (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '动态ID',

                               task_id BIGINT NOT NULL COMMENT '任务ID',

                               operator_id BIGINT NOT NULL COMMENT '操作用户',

                               activity_type VARCHAR(40) NOT NULL
                                   COMMENT 'CREATED/UPDATED/STATUS_CHANGED/ASSIGNEE_ADDED/ASSIGNEE_REMOVED',

                               content VARCHAR(500) DEFAULT NULL COMMENT '动态内容',

                               created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               KEY idx_activity_task_id ( task_id),
                                KEY idx_activity_created_time (created_time)

) COMMENT='任务动态日志表';

CREATE TABLE task_comment (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',

                              task_id BIGINT NOT NULL COMMENT '任务ID',

                              user_id BIGINT NOT NULL COMMENT '评论用户ID',

                              content VARCHAR(2000) NOT NULL COMMENT '评论内容',

                              created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,

                              deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                             KEY idx_task_comment_task_id (task_id),

                             KEY idx_task_comment_user_id (user_id),

                             KEY idx_task_comment_created_time (created_time)

) COMMENT='任务评论表';

CREATE TABLE notification (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',

                              user_id BIGINT NOT NULL COMMENT '接收用户ID',

                              type VARCHAR(50) NOT NULL COMMENT '通知类型',

                              title VARCHAR(200) NOT NULL COMMENT '通知标题',

                              content VARCHAR(1000) DEFAULT NULL COMMENT '通知内容',

                              related_type VARCHAR(50) DEFAULT NULL COMMENT '关联业务类型',

                              related_id BIGINT DEFAULT NULL COMMENT '关联业务ID',

                              is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',

                              read_time DATETIME DEFAULT NULL COMMENT '阅读时间',

                              created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              KEY idx_notification_user_read (user_id,is_read),

                              KEY idx_notification_created_time (created_time),

                              KEY idx_notification_related (related_type,related_id)

) COMMENT='站内通知表';