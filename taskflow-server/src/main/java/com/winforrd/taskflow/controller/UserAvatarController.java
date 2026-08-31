package com.winforrd.taskflow.controller;


import com.winforrd.taskflow.common.result.Result;

import com.winforrd.taskflow.service.UserAvatarService;

import com.winforrd.taskflow.vo.AvatarUploadVO;


import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;


/**
 * =========================================================
 * 用户头像
 * =========================================================
 */
@RestController
@RequestMapping("/api/users/avatar")
@RequiredArgsConstructor
public class UserAvatarController {


    private final UserAvatarService
            userAvatarService;


    /**
     * =====================================================
     * 上传 / 替换头像
     * =====================================================
     *
     * POST
     *
     * /api/users/avatar
     *
     * Content-Type:
     *
     * multipart/form-data
     */
    @PostMapping
    public Result<AvatarUploadVO>
    uploadAvatar(

            @RequestParam("file")
            MultipartFile file

    ) {


        String avatar =
                userAvatarService
                        .uploadAvatar(
                                file
                        );


        return Result.success(

                new AvatarUploadVO(
                        avatar
                )

        );
    }


    /**
     * =====================================================
     * 删除头像
     * =====================================================
     *
     * DELETE
     *
     * /api/users/avatar
     */
    @DeleteMapping
    public Result<Void>
    removeAvatar() {


        userAvatarService
                .removeAvatar();


        return Result.success();
    }


}