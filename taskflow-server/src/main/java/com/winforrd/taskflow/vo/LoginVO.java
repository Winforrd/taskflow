package com.winforrd.taskflow.vo;

import lombok.Data;

@Data
public class LoginVO {

    /**
     * token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录昵称
     */
    private String nickname;
}
