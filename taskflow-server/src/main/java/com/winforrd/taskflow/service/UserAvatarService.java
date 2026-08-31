package com.winforrd.taskflow.service;


import org.springframework.web.multipart.MultipartFile;


/**
 * =========================================================
 * 用户头像服务
 * =========================================================
 */
public interface UserAvatarService {


    /**
     * 上传 / 替换当前用户头像。
     *
     * @return 头像访问地址
     */
    String uploadAvatar(
            MultipartFile file
    );


    /**
     * 删除当前用户头像。
     */
    void removeAvatar();


}