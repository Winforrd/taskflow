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
public class AvatarResourceConfig
        implements WebMvcConfigurer {


    private final AvatarStorageProperties
            avatarStorageProperties;


    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {


        Path avatarPath =
                Paths.get(
                                avatarStorageProperties
                                        .getAvatarDir()
                        )
                        .toAbsolutePath()
                        .normalize();


        String location =
                avatarPath
                        .toUri()
                        .toString();


        String urlPrefix =
                normalizeUrlPrefix(
                        avatarStorageProperties
                                .getAvatarUrlPrefix()
                );


        registry
                .addResourceHandler(
                        urlPrefix + "**"
                )
                .addResourceLocations(
                        location
                );
    }


    /**
     * 保证：
     *
     * /uploads/avatars/
     *
     * 最后一定有 /
     */
    private String normalizeUrlPrefix(
            String prefix
    ) {


        if (
                prefix == null
                        ||
                        prefix.isBlank()
        ) {

            return "/uploads/avatars/";
        }


        String result =
                prefix.trim();


        if (
                !result.startsWith("/")
        ) {

            result =
                    "/" + result;
        }


        if (
                !result.endsWith("/")
        ) {

            result =
                    result + "/";
        }


        return result;
    }


}