package com.winforrd.taskflow.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;


import com.winforrd.taskflow.entity.OperationLog;

import com.winforrd.taskflow.mapper.OperationLogMapper;

import com.winforrd.taskflow.service.DataPermissionService;

import com.winforrd.taskflow.service.OperationLogService;

import com.winforrd.taskflow.vo.OperationLogVO;


import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;


import java.util.ArrayList;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    private final DataPermissionService dataPermissionService;

    /**
     * =====================================================
     * 查询项目日志
     * =====================================================
     */
    @Override
    public List<OperationLogVO> getProjectLogs(Long projectId, String module, Boolean success, String keyword) {
        // 检查项目查看权限
        dataPermissionService.checkProjectView(projectId);
        // 构建查询条件
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                                                    .eq(OperationLog::getProjectId, projectId)
                                                    .orderByDesc(OperationLog::getCreatedTime);
        // 模块过滤
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        // 成功失败过滤
        if (success != null) {
            wrapper.eq(OperationLog::getSuccess, success ? 1 : 0);
        }
        // 关键字过滤
        if (StringUtils.hasText(keyword)) {
            wrapper.and(query -> query.like(OperationLog::getUsername, keyword)
                                                                .or().like(OperationLog::getOperation, keyword)
                                                                .or().like(OperationLog::getRequestUri, keyword));
        }
        // 限制200条。
        wrapper.last("LIMIT 200");
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        // 转换VO
        List<OperationLogVO> result = new ArrayList<>();
        // 遍历转换
        for (OperationLog log : logs) {
            result.add(convertVO(log));
        }
        return result;
    }
    private OperationLogVO convertVO(OperationLog log) {
        // 创建VO对象
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setUsername(log.getUsername());
        vo.setTeamId(log.getTeamId());
        vo.setProjectId(log.getProjectId());
        vo.setTaskId(log.getTaskId());
        vo.setModule(log.getModule());
        vo.setOperation(log.getOperation());
        vo.setHttpMethod(log.getHttpMethod());
        vo.setRequestUri(log.getRequestUri());
        vo.setRequestParams(log.getRequestParams());
        vo.setIp(log.getIp());
        vo.setUserAgent(log.getUserAgent());
        vo.setSuccess(Integer.valueOf(1).equals(log.getSuccess()));
        vo.setErrorMessage(log.getErrorMessage());
        vo.setCostMs(log.getCostMs());
        vo.setCreatedTime(log.getCreatedTime());
        return vo;
    }
}