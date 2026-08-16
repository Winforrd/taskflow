package com.winforrd.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.winforrd.taskflow.entity.Project;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> { }