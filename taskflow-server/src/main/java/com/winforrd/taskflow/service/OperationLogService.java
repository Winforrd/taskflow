package com.winforrd.taskflow.service;


import com.winforrd.taskflow.vo.OperationLogVO;


import java.util.List;


public interface OperationLogService {


    /**
     * 查询项目操作日志。
     *
     * 最多返回最近200条。
     */
    List<OperationLogVO> getProjectLogs(Long projectId, String module, Boolean success, String keyword);


}