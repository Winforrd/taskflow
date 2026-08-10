package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.common.util.PasswordUtil;
import com.winforrd.taskflow.dto.ChangePasswordRequest;
import com.winforrd.taskflow.dto.UpdateProfileRequest;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.service.UserService;
import com.winforrd.taskflow.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;

    /**
     * 获取当前登录用户
     */
    private SysUser getCurrentUser() {
        //获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        //根据用户ID查询用户信息
        SysUser user = sysUserMapper.selectById(userId);
        //判断用户是否存在
        if (user == null) {
            throw new RuntimeException(
                    ErrorCode.USER_NOT_FOUND.getMessage()
            );
        }
        return user;
    }


    /**
     * 获取当前登录用户的信息
     */
    @Override
    public UserProfileVO getCurrentUserProfile() {
        // 获取当前登录用户
        SysUser user = getCurrentUser();
        // 构建并返回用户信息VO
        return UserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdTime(user.getCreatedTime())
                .build();
    }

    /**
     * 修改当前用户个人信息
     */
    @Override
    @Transactional
    public void updateCurrentUserProfile(UpdateProfileRequest request) {
        // 获取当前登录用户
        SysUser currentUser = getCurrentUser();
        // 更新用户信息
        // 如果填写邮箱，检查该邮箱是否被其他用户占用
        if(StringUtils.hasText(request.getEmail())){
            Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, request.getEmail())
                    .ne(SysUser::getId, currentUser.getId()));
            if(count > 0){
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUser.getId());
        updateUser.setNickname(request.getNickname());
        updateUser.setEmail(request.getEmail());
        updateUser.setPhone(request.getPhone());
        sysUserMapper.updateById(updateUser);
    }

    /**
     * 修改当前用户密码
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 获取当前登录用户
        SysUser currentUser = getCurrentUser();
        // 检查原密码是否正确
        boolean matches = PasswordUtil.matches(request.getOldPassword(), currentUser.getPassword());
        if (!matches) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }
        /*
         * 新密码长度校验
         *
         * 虽然 DTO 已校验，
         * Service再做关键业务保护也可以
         */
        if(request.getNewPassword() == null || request.getNewPassword().length() < 6){
            throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        // 校验新密码是否与原密码相同
        if (PasswordUtil.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BusinessException(ErrorCode.NEW_PASSWORD_SAME);
        }

        // BCrypt加密新密码
        String encodedPassword = PasswordUtil.encode(request.getNewPassword());

        // 更新用户密码
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUser.getId());
        updateUser.setPassword(encodedPassword);
        sysUserMapper.updateById(updateUser);

        /*
         * 修改密码后注销当前登录状态
         *
         * 强制重新登录
         */
        StpUtil.logout();

    }
}

