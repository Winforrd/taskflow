package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.ChangePasswordRequest;
import com.winforrd.taskflow.dto.UpdateProfileRequest;
import com.winforrd.taskflow.service.UserService;
import com.winforrd.taskflow.vo.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        // 调用服务层方法获取当前用户信息
        UserProfileVO profileVO = userService.getCurrentUserProfile();
        // 返回结果
        return Result.success(profileVO);
    }

    /**
     * 修改当前用户个人信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
        userService.updateCurrentUserProfile(request);
        return Result.success();
    }
    /**
     * 修改当前用户密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return Result.success();
    }

}
