package com.winforrd.taskflow.config;


import lombok.RequiredArgsConstructor;


import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * =========================================================
 * 头像静态资源访问
 * =========================================================
 *
 * 浏览器：
 *
 * http://localhost:8080/uploads/avatars/xxx.png
 *
 * 实际读取：
 *
 * ./uploads/avatars/xxx.png
 */
@Configuration
@RequiredArgsConstructor
public class AvatarResourceConfig implements WebMvcConfigurer {

    private final AvatarStorageProperties avatarStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 头像静态资源访问
        Path avatarPath = Paths.get(avatarStorageProperties.getAvatarDir()).toAbsolutePath().normalize();
        // 静态资源位置
        String location = avatarPath.toUri().toString();
        // 静态资源访问前缀
        String urlPrefix = normalizeUrlPrefix(avatarStorageProperties.getAvatarUrlPrefix());
        // 配置静态资源访问
        registry.addResourceHandler(urlPrefix + "**").addResourceLocations(location);
    }

    /**
     * 保证：
     *
     * /uploads/avatars/
     *
     * 最后一定有 /
     */
    private String normalizeUrlPrefix(String prefix) {
        // 前缀为空或blank则返回默认前缀
        if (prefix == null || prefix.isBlank()) {
            return "/uploads/avatars/";
        }
        // 去除前缀空格并确保以 / 开头
        String result = prefix.trim();
        if (!result.startsWith("/")) {
            result = "/" + result;
        }
        // 确保以 / 结尾
        if (!result.endsWith("/")) {result = result + "/";}
        return result;
    }


}