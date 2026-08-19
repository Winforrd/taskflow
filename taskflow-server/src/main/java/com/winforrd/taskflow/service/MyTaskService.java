package com.winforrd.taskflow.service;

import com.winforrd.taskflow.common.result.PageResult;
import com.winforrd.taskflow.dto.MyTaskQuery;
import com.winforrd.taskflow.vo.MyTaskVO;

public interface MyTaskService {

    /**
     * 查询我的任务
     */
    PageResult<MyTaskVO> getMyTasks(MyTaskQuery query);
}