package com.winforrd.taskflow.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.exception.BusinessException;
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
    /**
     * 用户注册
     */
    @Override
    public void register(RegisterRequest request) {

        //1. 参数校验
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT);
        }
        //2. 查询用户名是否存在
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        //3. 创建用户
        SysUser user = new SysUser();user.setUsername(request.getUsername());
        // BCrypt加密密码
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        //正常状态
        user.setStatus(1);
        sysUserMapper.insert(user);

    }

    /**
     * 用户登录
     */
    @Override
    public LoginVO login(LoginRequest request) {
        //1. 查询用户
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        //2. 校验密码
        boolean result = PasswordUtil.matches(request.getPassword(), user.getPassword());
        if (!result) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        //3. 校验账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);

        }
        //4. Sa-Token登录
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenInfo().getTokenValue();
        //5. 返回VO
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        return vo;
    }
}