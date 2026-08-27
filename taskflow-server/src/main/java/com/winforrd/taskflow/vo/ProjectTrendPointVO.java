package com.winforrd.taskflow.vo;


import lombok.Data;


import java.time.LocalDate;


@Data
public class ProjectTrendPointVO {


    /**
     * 日期
     */
    private LocalDate date;


    /**
     * 当天完成任务数
     */
    private Long completedCount;


}