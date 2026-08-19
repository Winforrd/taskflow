package com.winforrd.taskflow.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.winforrd.taskflow.vo.MyTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MyTaskMapper {

    /**
     * 我负责的任务
     */
    IPage<MyTaskVO> selectAssignedTasks(Page<MyTaskVO> page, @Param("userId") Long userId, @Param("keyword") String keyword, @Param("status") String status);

    /**
     * 我创建的任务
     */
    IPage<MyTaskVO> selectCreatedTasks(Page<MyTaskVO> page, @Param("userId") Long userId, @Param("keyword") String keyword, @Param("status") String status);

    /**
     * 待我验收
     */
    IPage<MyTaskVO> selectReviewTasks(Page<MyTaskVO> page, @Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 我负责且已经逾期
     */
    IPage<MyTaskVO> selectOverdueTasks(Page<MyTaskVO> page, @Param("userId") Long userId, @Param("keyword") String keyword);
}