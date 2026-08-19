package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.winforrd.taskflow.common.enums.MyTaskType;
import com.winforrd.taskflow.common.enums.TaskStatus;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.common.result.PageResult;
import com.winforrd.taskflow.dto.MyTaskQuery;
import com.winforrd.taskflow.mapper.MyTaskMapper;
import com.winforrd.taskflow.service.MyTaskService;
import com.winforrd.taskflow.vo.MyTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MyTaskServiceImpl
        implements MyTaskService {

    private final MyTaskMapper myTaskMapper;


    /**
     * =====================================================
     * 查询我的任务
     * =====================================================
     */
    @Override
    public PageResult<MyTaskVO> getMyTasks(
            MyTaskQuery query
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        /**
         * =================================================
         * 1. 查询类型
         * =================================================
         *
         * 如果前端没有传 type，
         * 默认查询“我负责的任务”。
         */
        MyTaskType type =
                query.getType() == null

                        ? MyTaskType.ASSIGNED

                        : query.getType();


        /**
         * =================================================
         * 2. 清理关键词
         * =================================================
         */
        String keyword =
                StringUtils.hasText(
                        query.getKeyword()
                )

                        ? query.getKeyword().trim()

                        : null;


        /**
         * =================================================
         * 3. 校验任务状态
         * =================================================
         */
        String status =
                validateStatus(
                        query.getStatus()
                );


        /**
         * =================================================
         * 4. 创建分页对象
         * =================================================
         */
        Page<MyTaskVO> page =
                new Page<>(
                        query.getPageNum(),
                        query.getPageSize()
                );


        /**
         * =================================================
         * 重要修复
         * =================================================
         *
         * MyTaskMapper.xml 中的 SQL 比较复杂：
         *
         * JOIN
         * +
         * EXISTS
         * +
         * 多次使用 #{userId}
         *
         * MyBatis-Plus 默认会尝试优化 COUNT SQL。
         *
         * 有时优化过程中会删除 JOIN 或 WHERE 条件，
         * 但是参数映射仍然保留，
         * 最终出现：
         *
         * Parameter index out of range
         *
         * 所以这里关闭 COUNT SQL 自动优化。
         *
         * 注意：
         *
         * 不是关闭分页，
         * 也不是关闭 COUNT。
         *
         * 只是告诉 MyBatis-Plus：
         *
         * “按照原 SQL 结构生成 COUNT，
         * 不要擅自优化。”
         */
        page.setOptimizeCountSql(
                false
        );


        /**
         * 同时关闭 JOIN COUNT 优化。
         *
         * 对复杂 JOIN 查询更加稳妥。
         */
        page.setOptimizeJoinOfCountSql(
                false
        );


        IPage<MyTaskVO> result;


        /**
         * =================================================
         * 5. 根据任务分类执行不同查询
         * =================================================
         */
        switch (type) {

            /**
             * ---------------------------------------------
             * 我负责的
             * ---------------------------------------------
             */
            case ASSIGNED ->

                    result =
                            myTaskMapper
                                    .selectAssignedTasks(

                                            page,

                                            currentUserId,

                                            keyword,

                                            status
                                    );


            /**
             * ---------------------------------------------
             * 我创建的
             * ---------------------------------------------
             */
            case CREATED ->

                    result =
                            myTaskMapper
                                    .selectCreatedTasks(

                                            page,

                                            currentUserId,

                                            keyword,

                                            status
                                    );


            /**
             * ---------------------------------------------
             * 待我验收
             * ---------------------------------------------
             *
             * 状态已经固定：
             *
             * IN_REVIEW
             *
             * 所以不需要额外传 status。
             */
            case REVIEW ->

                    result =
                            myTaskMapper
                                    .selectReviewTasks(

                                            page,

                                            currentUserId,

                                            keyword
                                    );


            /**
             * ---------------------------------------------
             * 已逾期
             * ---------------------------------------------
             *
             * SQL 已经固定：
             *
             * deadline < 当前日期
             *
             * 并且：
             *
             * status != DONE
             * status != CANCELLED
             */
            case OVERDUE ->

                    result =
                            myTaskMapper
                                    .selectOverdueTasks(

                                            page,

                                            currentUserId,

                                            keyword
                                    );


            default ->

                    throw new BusinessException(
                            "任务查询类型不正确"
                    );
        }


        /**
         * =================================================
         * 6. 转换分页结果
         * =================================================
         */
        return PageResult.of(
                result
        );
    }


    /**
     * =====================================================
     * 校验状态参数
     * =====================================================
     */
    private String validateStatus(
            String status
    ) {

        /**
         * 没传状态：
         * 查询全部。
         */
        if (
                !StringUtils.hasText(
                        status
                )
        ) {

            return null;
        }


        /**
         * 转大写，
         * 防止前端传：
         *
         * todo
         * Todo
         *
         * 等格式。
         */
        String value =
                status
                        .trim()
                        .toUpperCase();


        /**
         * 判断是不是合法的 TaskStatus。
         */
        for (
                TaskStatus taskStatus :
                TaskStatus.values()
        ) {

            if (
                    taskStatus.name()
                            .equals(
                                    value
                            )
            ) {

                return value;
            }
        }


        throw new BusinessException(
                "任务状态参数不正确"
        );
    }
}