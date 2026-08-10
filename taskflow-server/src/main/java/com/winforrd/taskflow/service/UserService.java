package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.ChangePasswordRequest;
import com.winforrd.taskflow.dto.UpdateProfileRequest;
import com.winforrd.taskflow.vo.UserProfileVO;

public interface UserService {

    /**
     * 获取当前用户的信息
     */
    UserProfileVO getCurrentUserProfile();

    /**
     * 修改当前用户个人信息
     */
    void updateCurrentUserProfile(UpdateProfileRequest request);

    /**
     * 修改当前用户密码
     */
    void changePassword(ChangePasswordRequest request);
}
