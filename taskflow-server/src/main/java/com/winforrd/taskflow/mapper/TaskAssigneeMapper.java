package com.winforrd.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.winforrd.taskflow.entity.TaskAssignee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskAssigneeMapper extends BaseMapper<TaskAssignee> { }