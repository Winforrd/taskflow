package com.winforrd.taskflow.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;


import com.winforrd.taskflow.entity.OperationLog;

import com.winforrd.taskflow.mapper.OperationLogMapper;

import com.winforrd.taskflow.service.DataPermissionService;

import com.winforrd.taskflow.service.OperationLogService;

import com.winforrd.taskflow.vo.OperationLogVO;


import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;


import java.util.ArrayList;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl
        implements OperationLogService {


    private final OperationLogMapper
            operationLogMapper;


    private final DataPermissionService
            dataPermissionService;


    /**
     * =====================================================
     * 查询项目日志
     * =====================================================
     */
    @Override
    public List<OperationLogVO> getProjectLogs(

            Long projectId,

            String module,

            Boolean success,

            String keyword

    ) {


        /**
         * =================================================
         * 数据权限。
         *
         * 用户不能通过修改URL：
         *
         * /projects/999/logs
         *
         * 查看自己无权访问的项目。
         * =================================================
         */
        dataPermissionService.checkProjectView(
                projectId
        );


        LambdaQueryWrapper<OperationLog> wrapper =
                new LambdaQueryWrapper<OperationLog>()

                        .eq(
                                OperationLog::getProjectId,
                                projectId
                        )

                        .orderByDesc(
                                OperationLog::getCreatedTime
                        );


        if (
                StringUtils.hasText(
                        module
                )
        ) {

            wrapper.eq(
                    OperationLog::getModule,
                    module
            );
        }


        if (success != null) {

            wrapper.eq(
                    OperationLog::getSuccess,
                    success ? 1 : 0
            );
        }


        if (
                StringUtils.hasText(
                        keyword
                )
        ) {

            wrapper.and(
                    query ->

                            query.like(
                                            OperationLog::getUsername,
                                            keyword
                                    )

                                    .or()

                                    .like(
                                            OperationLog::getOperation,
                                            keyword
                                    )

                                    .or()

                                    .like(
                                            OperationLog::getRequestUri,
                                            keyword
                                    )
            );
        }


        // 限制200条。
        wrapper.last("LIMIT 200");


        List<OperationLog> logs =
                operationLogMapper.selectList(
                        wrapper
                );


        List<OperationLogVO> result =
                new ArrayList<>();


        for (
                OperationLog log :
                logs
        ) {

            result.add(
                    convertVO(
                            log
                    )
            );
        }


        return result;
    }


    private OperationLogVO convertVO(
            OperationLog log
    ) {


        OperationLogVO vo =
                new OperationLogVO();


        vo.setId(
                log.getId()
        );


        vo.setUserId(
                log.getUserId()
        );


        vo.setUsername(
                log.getUsername()
        );


        vo.setTeamId(
                log.getTeamId()
        );


        vo.setProjectId(
                log.getProjectId()
        );


        vo.setTaskId(
                log.getTaskId()
        );


        vo.setModule(
                log.getModule()
        );


        vo.setOperation(
                log.getOperation()
        );


        vo.setHttpMethod(
                log.getHttpMethod()
        );


        vo.setRequestUri(
                log.getRequestUri()
        );


        vo.setRequestParams(
                log.getRequestParams()
        );


        vo.setIp(
                log.getIp()
        );


        vo.setUserAgent(
                log.getUserAgent()
        );


        vo.setSuccess(
                Integer.valueOf(1)
                        .equals(
                                log.getSuccess()
                        )
        );


        vo.setErrorMessage(
                log.getErrorMessage()
        );


        vo.setCostMs(
                log.getCostMs()
        );


        vo.setCreatedTime(
                log.getCreatedTime()
        );


        return vo;
    }


}