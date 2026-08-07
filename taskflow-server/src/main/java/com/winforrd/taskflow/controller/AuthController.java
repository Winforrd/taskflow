package com.winforrd.taskflow.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.LoginRequest;
import com.winforrd.taskflow.dto.RegisterRequest;
import com.winforrd.taskflow.service.AuthService;
import com.winforrd.taskflow.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 注册
     *
     * @param request
     * @return
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    /**
     * 登录
     *
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginRequest request) {
        LoginVO vo = authService.login(request);
        return Result.success(vo);
    }

    /**
     * 登出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }


}
