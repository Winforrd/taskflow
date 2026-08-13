package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.CreateTeamRequest;
import com.winforrd.taskflow.dto.UpdateTeamRequest;
import com.winforrd.taskflow.vo.TeamDetailVO;
import com.winforrd.taskflow.vo.TeamVO;

import java.util.List;

public interface TeamService {

    // 创建团队
    Long createTeam(CreateTeamRequest request);

    // 获取我的团队列表
    List<TeamVO> getMyTeams();

    // 获取团队详情
    TeamDetailVO getTeamDetail(Long teamId);

    // 更新团队信息
    void updateTeam(Long teamId, UpdateTeamRequest request);
}