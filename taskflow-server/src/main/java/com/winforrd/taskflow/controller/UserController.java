package com.winforrd.taskflow.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;

    @GetMapping("/profile")
    public Result<SysUser> profile() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        // 查询用户信息
        SysUser user = userMapper.selectById(userId);
        // 不返回密码
        user.setPassword(null);
        return Result.success(user);
    }

}
