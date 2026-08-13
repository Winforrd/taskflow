package com.winforrd.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.winforrd.taskflow.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
}
