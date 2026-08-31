package com.winforrd.taskflow.config;


import lombok.Data;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * =========================================================
 * 用户头像文件存储配置
 * =========================================================
 *
 * application.yml：
 *
 * taskflow:
 *   upload:
 *     avatar-dir: ./uploads/avatars
 *     avatar-url-prefix: /uploads/avatars/
 */
@Data
@Component
@ConfigurationProperties(prefix = "taskflow.upload")
public class AvatarStorageProperties {

    /**
     * 头像实际存储目录。
     *
     * 开发环境默认：
     *
     * taskflow-server/uploads/avatars/
     */
    private String avatarDir = "./uploads/avatars";


    /**
     * 浏览器访问前缀。
     */
    private String avatarUrlPrefix = "/uploads/avatars/";


}