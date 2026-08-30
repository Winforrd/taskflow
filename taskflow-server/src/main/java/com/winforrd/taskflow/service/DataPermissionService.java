package com.winforrd.taskflow.service;


public interface DataPermissionService {


    /**
     * 当前用户是否能够查看项目数据。
     *
     * 无权限直接抛业务异常。
     */
    void checkProjectView(Long projectId);


    /**
     * 当前用户是否能够查看任务数据。
     *
     * 根据task找到project，
     * 然后继续走项目数据权限。
     */
    void checkTaskView(Long taskId);


}