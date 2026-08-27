package com.winforrd.taskflow.vo;


import lombok.Data;


@Data
public class MemberWorkloadVO {


    /**
     * 用户ID
     */
    private Long userId;


    /**
     * 成员显示名称
     */
    private String memberName;


    /**
     * 头像
     */
    private String avatar;


    /**
     * 当前负责的任务总数
     *
     * 包括不同状态。
     */
    private Long assignedCount;


    /**
     * 待处理
     */
    private Long todoCount;


    /**
     * 进行中
     */
    private Long inProgressCount;


    /**
     * 待验收
     */
    private Long inReviewCount;


    /**
     * 已完成
     */
    private Long doneCount;


    /**
     * 已取消
     */
    private Long cancelledCount;


    /**
     * 当前逾期任务
     */
    private Long overdueCount;


}