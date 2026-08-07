package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.LoginRequest;
import com.winforrd.taskflow.dto.RegisterRequest;
import com.winforrd.taskflow.vo.LoginVO;

public interface AuthService {

    /**
     * 注册
     * @param request
     * @return
     */
    void register(RegisterRequest request);

    /**
     * 登录
     * @param request
     * @return
     */
    LoginVO login(LoginRequest request);
}
