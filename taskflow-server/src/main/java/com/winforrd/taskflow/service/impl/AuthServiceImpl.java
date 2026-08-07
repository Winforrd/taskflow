package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.winforrd.taskflow.common.util.PasswordUtil;
import com.winforrd.taskflow.dto.LoginRequest;
import com.winforrd.taskflow.dto.RegisterRequest;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.service.AuthService;
import com.winforrd.taskflow.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    @Override
    public void register(RegisterRequest request) {

        //验证密码
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        //1. 查询用户名是否存在
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (count > 0){
            throw new RuntimeException("用户名已存在");
        }
        //2. 创建用户
        SysUser User = new SysUser();
        //密码加密
        User.setPassword(PasswordUtil.encode(request.getPassword()));
        User.setUsername(request.getUsername());
        User.setNickname(request.getNickname());
        User.setStatus(1);
        sysUserMapper.insert(User);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        //1. 验证用户名是否存在
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        //2. 验证密码
        boolean result = PasswordUtil.matches(request.getPassword(), user.getPassword());
        if (!result){
            throw new RuntimeException("密码错误");
        }
        //3. 验证账号状态
        if (user.getStatus() == 0){
            throw new RuntimeException("账号已被禁用");
        }
        //4. Sa-Token 登录,生成会话 Token
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenInfo().getTokenValue();
        //5. 封装VO返回
        LoginVO vo=new LoginVO();

        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        return vo;
    }
}
