package com.winforrd.taskflow.service;


import com.winforrd.taskflow.vo.OperationLogVO;


import java.util.List;


public interface OperationLogService {
    /**
     * 获取项目操作日志
     * @param projectId 项目ID
     * @param module 模块
     * @param success 是否成功
     * @param keyword 关键字
     * @return 操作日志列表
     */
    List<OperationLogVO> getProjectLogs(Long projectId, String module, Boolean success, String keyword);
}