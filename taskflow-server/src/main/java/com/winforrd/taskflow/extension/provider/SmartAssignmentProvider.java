package com.winforrd.taskflow.extension.provider;


import com.winforrd.taskflow.extension.ExtensionProvider;


import java.util.List;


/**
 * =========================================================
 * 智能任务分派扩展接口
 * =========================================================
 *
 * 后续可以根据：
 *
 * 1. 当前工作量
 * 2. 历史完成率
 * 3. 技能标签
 * 4. 任务优先级
 * 5. 截止时间
 *
 * 对负责人进行推荐。
 *
 *
 * 第一版这里只预留：
 *
 * 输入：
 * 项目
 * 任务
 * 推荐人数
 *
 * 输出：
 * 推荐用户ID顺序
 */
public interface SmartAssignmentProvider
        extends ExtensionProvider {


    List<Long> recommend(

            Long projectId,

            Long taskId,

            Integer limit

    );


}