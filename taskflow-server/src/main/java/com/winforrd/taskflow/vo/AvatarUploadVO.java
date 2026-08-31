package com.winforrd.taskflow.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * =========================================================
 * 头像上传结果
 * =========================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUploadVO {


    /**
     * 数据库存储的头像路径。
     *
     * 例如：
     *
     * /uploads/avatars/abc.png
     */
    private String avatar;


}