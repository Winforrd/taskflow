package com.winforrd.taskflow.aspect;


import cn.dev33.satoken.stp.StpUtil;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import com.winforrd.taskflow.entity.OperationLog;
import com.winforrd.taskflow.entity.Project;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.entity.Task;


import com.winforrd.taskflow.mapper.OperationLogMapper;
import com.winforrd.taskflow.mapper.ProjectMapper;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.mapper.TaskMapper;


import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * =========================================================
 * 系统操作日志AOP
 * =========================================================
 *
 * 功能：
 *
 * 1. 自动拦截Controller写操作
 * 2. 记录操作用户
 * 3. 记录HTTP方法
 * 4. 记录URI
 * 5. 记录请求参数
 * 6. 记录IP
 * 7. 记录User-Agent
 * 8. 记录执行耗时
 * 9. 记录成功/失败
 * 10. 自动关联teamId/projectId/taskId
 *
 *
 * 特别处理：
 *
 * 创建任务：
 *
 * POST /api/projects/{projectId}/tasks
 *
 * URL里没有taskId，
 * 所以创建成功后从Controller返回结果中取得新taskId。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {


    private final OperationLogMapper
            operationLogMapper;


    private final TaskMapper
            taskMapper;


    private final ProjectMapper
            projectMapper;


    private final SysUserMapper
            sysUserMapper;


    private final ObjectMapper
            objectMapper;


    /**
     * =====================================================
     * URI ID提取规则
     * =====================================================
     */

    private static final Pattern TEAM_PATTERN =
            Pattern.compile(
                    "/api/teams/(\\d+)"
            );


    private static final Pattern PROJECT_PATTERN =
            Pattern.compile(
                    "/api/projects/(\\d+)"
            );


    private static final Pattern TASK_PATTERN =
            Pattern.compile(
                    "/api/tasks/(\\d+)"
            );


    /**
     * 创建任务接口：
     *
     * POST
     * /api/projects/{projectId}/tasks
     */
    private static final Pattern CREATE_TASK_PATTERN =
            Pattern.compile(
                    "^/api/projects/\\d+/tasks/?$"
            );


    /**
     * =====================================================
     * 拦截Controller
     * =====================================================
     */
    @Around(
            "execution(public * com.winforrd.taskflow.controller..*(..))"
    )
    public Object aroundController(
            ProceedingJoinPoint joinPoint
    ) throws Throwable {


        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder
                                .getRequestAttributes();


        /**
         * 非HTTP请求直接执行。
         */
        if (attributes == null) {

            return joinPoint.proceed();
        }


        HttpServletRequest request =
                attributes.getRequest();


        String method =
                request
                        .getMethod()
                        .toUpperCase(
                                Locale.ROOT
                        );


        String uri =
                request.getRequestURI();


        /**
         * =================================================
         * 查询类操作不记录操作日志
         * =================================================
         */
        if (
                "GET".equals(method)
                        ||
                        "HEAD".equals(method)
                        ||
                        "OPTIONS".equals(method)
        ) {

            return joinPoint.proceed();
        }


        /**
         * =================================================
         * 认证接口不记录
         *
         * 防止登录密码等敏感参数进入操作日志。
         * =================================================
         */
        if (
                uri.startsWith(
                        "/api/auth"
                )
        ) {

            return joinPoint.proceed();
        }


        long startTime =
                System.currentTimeMillis();


        Throwable businessError =
                null;


        /**
         * =================================================
         * 新增：
         *
         * 保存Controller实际返回结果。
         *
         * 创建任务的时候，
         * taskId就在返回结果data里面。
         * =================================================
         */
        Object result =
                null;


        try {


            result =
                    joinPoint.proceed();


            return result;


        }
        catch (Throwable throwable) {


            businessError =
                    throwable;


            throw throwable;


        }
        finally {


            long costMs =
                    System.currentTimeMillis()
                            -
                            startTime;


            try {


                saveOperationLog(

                        joinPoint,

                        request,

                        method,

                        uri,

                        costMs,

                        businessError,

                        /**
                         * 关键：
                         *
                         * 把Controller返回值传给日志方法。
                         */
                        result

                );


            }
            catch (Exception logException) {


                /**
                 * 日志记录失败绝不能影响正常业务。
                 */
                log.error(
                        "保存操作日志失败",
                        logException
                );
            }
        }
    }


    /**
     * =====================================================
     * 保存操作日志
     * =====================================================
     */
    private void saveOperationLog(

            ProceedingJoinPoint joinPoint,

            HttpServletRequest request,

            String method,

            String uri,

            long costMs,

            Throwable throwable,

            Object result

    ) {


        OperationLog operationLog =
                new OperationLog();


        /**
         * HTTP方法
         */
        operationLog.setHttpMethod(
                method
        );


        /**
         * 请求地址
         */
        operationLog.setRequestUri(
                limit(
                        uri,
                        500
                )
        );


        /**
         * IP
         */
        operationLog.setIp(
                getClientIp(
                        request
                )
        );


        /**
         * 浏览器
         */
        operationLog.setUserAgent(

                limit(

                        request.getHeader(
                                "User-Agent"
                        ),

                        500

                )
        );


        /**
         * 耗时
         */
        operationLog.setCostMs(
                costMs
        );


        operationLog.setCreatedTime(
                LocalDateTime.now()
        );


        /**
         * 模块
         */
        operationLog.setModule(
                resolveModule(
                        uri
                )
        );


        /**
         * 操作名称
         */
        operationLog.setOperation(

                resolveOperation(

                        method,

                        uri

                )
        );


        /**
         * 当前登录用户
         */
        fillUser(
                operationLog
        );


        /**
         * =================================================
         * 业务关联信息
         *
         * 关键修改：
         *
         * 把method和Controller返回结果一起传进去。
         * =================================================
         */
        fillBusinessContext(

                operationLog,

                method,

                uri,

                result

        );


        /**
         * 请求参数
         */
        operationLog.setRequestParams(

                serializeArguments(
                        joinPoint.getArgs()
                )
        );


        /**
         * =================================================
         * 成功 / 失败
         * =================================================
         */
        if (throwable == null) {


            operationLog.setSuccess(
                    1
            );


        }
        else {


            operationLog.setSuccess(
                    0
            );


            operationLog.setErrorMessage(

                    limit(

                            throwable.getMessage(),

                            1000

                    )
            );
        }


        /**
         * 保存
         */
        operationLogMapper.insert(
                operationLog
        );
    }


    /**
     * =====================================================
     * 当前用户
     * =====================================================
     */
    private void fillUser(
            OperationLog logEntity
    ) {


        /**
         * 没登录时不处理。
         */
        if (
                !StpUtil.isLogin()
        ) {

            return;
        }


        Long userId =
                StpUtil.getLoginIdAsLong();


        logEntity.setUserId(
                userId
        );


        SysUser user =
                sysUserMapper.selectById(
                        userId
                );


        if (user == null) {

            return;
        }


        /**
         * 优先显示昵称。
         */
        if (
                user.getNickname() != null
                        &&
                        !user
                                .getNickname()
                                .isBlank()
        ) {


            logEntity.setUsername(
                    user.getNickname()
            );


        }
        else {


            logEntity.setUsername(
                    user.getUsername()
            );
        }
    }


    /**
     * =====================================================
     * 填充业务关联信息
     * =====================================================
     *
     * 普通任务操作：
     *
     * /api/tasks/10/status
     *
     * 可以直接从URI得到taskId=10。
     *
     *
     * 创建任务：
     *
     * /api/projects/4/tasks
     *
     * URI只能得到projectId=4。
     *
     * 创建成功以后：
     *
     * Controller：
     *
     * Result<Long>
     *
     * data = 新任务ID
     *
     * 所以这里额外从result取得taskId。
     */
    private void fillBusinessContext(

            OperationLog logEntity,

            String method,

            String uri,

            Object result

    ) {


        /**
         * =================================================
         * 第一步：
         *
         * 先从URI获取ID。
         * =================================================
         */

        Long taskId =
                extractId(

                        TASK_PATTERN,

                        uri

                );


        Long projectId =
                extractId(

                        PROJECT_PATTERN,

                        uri

                );


        Long teamId =
                extractId(

                        TEAM_PATTERN,

                        uri

                );


        /**
         * =================================================
         * 第二步：
         *
         * 创建任务属于特殊情况。
         *
         * POST /api/projects/{projectId}/tasks
         *
         * URL里面没有taskId。
         *
         * Controller返回：
         *
         * {
         *     code: ...,
         *     data: 新任务ID
         * }
         *
         * 从返回值中取得taskId。
         * =================================================
         */
        if (
                taskId == null
                        &&
                        "POST".equals(
                                method
                        )
                        &&
                        CREATE_TASK_PATTERN
                                .matcher(
                                        uri
                                )
                                .matches()
        ) {


            taskId =
                    extractCreatedIdFromResult(
                            result
                    );
        }


        /**
         * =================================================
         * 第三步：
         *
         * 如果拿到了taskId，
         * 保存taskId并反查project。
         * =================================================
         */
        if (taskId != null) {


            logEntity.setTaskId(
                    taskId
            );


            Task task =
                    taskMapper.selectById(
                            taskId
                    );


            if (task != null) {


                projectId =
                        task.getProjectId();
            }
        }


        /**
         * =================================================
         * 第四步：
         *
         * 项目ID。
         *
         * 再通过项目找到团队ID。
         * =================================================
         */
        if (projectId != null) {


            logEntity.setProjectId(
                    projectId
            );


            Project project =
                    projectMapper.selectById(
                            projectId
                    );


            if (project != null) {


                teamId =
                        project.getTeamId();
            }
        }


        /**
         * 团队ID
         */
        if (teamId != null) {


            logEntity.setTeamId(
                    teamId
            );
        }
    }


    /**
     * =====================================================
     * 从Controller返回结果获取新创建对象ID
     * =====================================================
     *
     * 当前任务创建接口：
     *
     * Result<Long>
     *
     * 最终JSON结构类似：
     *
     * {
     *     "code": 200,
     *     "message": "success",
     *     "data": 25
     * }
     *
     * 为了不强依赖Result类内部getter名称，
     * 直接通过Jackson转换成JsonNode，
     * 然后读取data。
     */
    private Long extractCreatedIdFromResult(
            Object result
    ) {


        if (result == null) {

            return null;
        }


        try {


            JsonNode root =
                    objectMapper.valueToTree(
                            result
                    );


            if (
                    root == null
                            ||
                            !root.has(
                                    "data"
                            )
            ) {

                return null;
            }


            JsonNode dataNode =
                    root.get(
                            "data"
                    );


            if (
                    dataNode == null
                            ||
                            dataNode.isNull()
            ) {

                return null;
            }


            /**
             * 正常情况：
             *
             * data = 25
             */
            if (
                    dataNode.isIntegralNumber()
            ) {


                return dataNode.longValue();
            }


            /**
             * 防御：
             *
             * 某些返回结构可能：
             *
             * data = "25"
             */
            if (
                    dataNode.isTextual()
            ) {


                String value =
                        dataNode.asText();


                if (
                        value != null
                                &&
                                !value.isBlank()
                ) {


                    return Long.valueOf(
                            value
                    );
                }
            }


        }
        catch (Exception exception) {


            /**
             * 日志上下文解析失败，
             * 不能影响正常业务。
             */
            log.debug(
                    "从Controller返回值解析新建任务ID失败",
                    exception
            );
        }


        return null;
    }


    /**
     * =====================================================
     * 从URI提取ID
     * =====================================================
     */
    private Long extractId(

            Pattern pattern,

            String uri

    ) {


        Matcher matcher =
                pattern.matcher(
                        uri
                );


        if (
                !matcher.find()
        ) {

            return null;
        }


        try {


            return Long.valueOf(

                    matcher.group(
                            1
                    )
            );


        }
        catch (NumberFormatException exception) {


            return null;
        }
    }


    /**
     * =====================================================
     * 模块识别
     * =====================================================
     */
    private String resolveModule(
            String uri
    ) {


        /**
         * /api/tasks/**
         */
        if (
                uri.startsWith(
                        "/api/tasks/"
                )
        ) {


            if (
                    uri.contains(
                            "/comments"
                    )
            ) {

                return "COMMENT";
            }


            return "TASK";
        }


        /**
         * /api/projects/**
         */
        if (
                uri.startsWith(
                        "/api/projects/"
                )
        ) {


            /**
             * 项目任务。
             */
            if (
                    uri.contains(
                            "/tasks"
                    )
            ) {

                return "TASK";
            }


            /**
             * 项目成员。
             */
            if (
                    uri.contains(
                            "/members"
                    )
            ) {

                return "PROJECT";
            }


            return "PROJECT";
        }


        /**
         * 团队
         */
        if (
                uri.startsWith(
                        "/api/teams/"
                )
        ) {

            return "TEAM";
        }


        /**
         * 通知
         */
        if (
                uri.startsWith(
                        "/api/notifications"
                )
        ) {

            return "NOTIFICATION";
        }


        /**
         * 用户
         */
        if (
                uri.startsWith(
                        "/api/users"
                )
                        ||
                        uri.startsWith(
                                "/api/profile"
                        )
        ) {

            return "USER";
        }


        return "OTHER";
    }


    /**
     * =====================================================
     * 操作名称识别
     * =====================================================
     */
    private String resolveOperation(

            String method,

            String uri

    ) {


        /**
         * =================================================
         * 修改任务状态
         * =================================================
         */
        if (
                uri.matches(
                        "/api/tasks/\\d+/status/?"
                )
        ) {

            return "变更任务状态";
        }


        /**
         * =================================================
         * 任务负责人
         * =================================================
         */
        if (
                uri.matches(
                        "/api/tasks/\\d+/assignees/\\d+/?"
                )
        ) {


            return "POST".equals(
                    method
            )

                    ? "添加任务负责人"

                    : "移除任务负责人";
        }


        /**
         * =================================================
         * 评论
         * =================================================
         */
        if (
                uri.contains(
                        "/comments"
                )
        ) {


            if (
                    "POST".equals(
                            method
                    )
            ) {

                return "发表评论";
            }


            if (
                    "DELETE".equals(
                            method
                    )
            ) {

                return "删除评论";
            }
        }


        /**
         * =================================================
         * 创建任务
         * =================================================
         */
        if (
                CREATE_TASK_PATTERN
                        .matcher(
                                uri
                        )
                        .matches()
                        &&
                        "POST".equals(
                                method
                        )
        ) {

            return "创建任务";
        }


        /**
         * =================================================
         * 修改任务
         * =================================================
         */
        if (
                uri.matches(
                        "/api/tasks/\\d+/?"
                )
                        &&
                        (
                                "PUT".equals(
                                        method
                                )
                                        ||
                                        "PATCH".equals(
                                                method
                                        )
                        )
        ) {

            return "修改任务";
        }


        /**
         * =================================================
         * 项目成员
         * =================================================
         */
        if (
                uri.contains(
                        "/members"
                )
                        &&
                        uri.startsWith(
                                "/api/projects/"
                        )
        ) {


            return switch (method) {


                case "POST" ->
                        "添加项目成员";


                case "PUT",
                     "PATCH" ->
                        "修改项目成员";


                case "DELETE" ->
                        "移除项目成员";


                default ->
                        "项目成员操作";
            };
        }


        /**
         * =================================================
         * 通知
         * =================================================
         */
        if (
                uri.startsWith(
                        "/api/notifications"
                )
        ) {


            return uri.endsWith(
                    "/read-all"
            )

                    ? "全部通知已读"

                    : "标记通知已读";
        }


        /**
         * =================================================
         * 默认
         * =================================================
         */
        return switch (method) {


            case "POST" ->
                    "新增数据";


            case "PUT",
                 "PATCH" ->
                    "修改数据";


            case "DELETE" ->
                    "删除数据";


            default ->
                    "数据操作";
        };
    }


    /**
     * =====================================================
     * 请求参数序列化
     * =====================================================
     */
    private String serializeArguments(
            Object[] args
    ) {


        if (
                args == null
                        ||
                        args.length == 0
        ) {

            return null;
        }


        List<Object> safeArgs =
                new ArrayList<>();


        for (
                Object arg :
                args
        ) {


            if (arg == null) {


                safeArgs.add(
                        null
                );


                continue;
            }


            /**
             * Servlet对象不写日志。
             */
            if (
                    arg instanceof ServletRequest
                            ||
                            arg instanceof ServletResponse
                            ||
                            arg instanceof MultipartFile
                            ||
                            arg instanceof MultipartFile[]
            ) {

                continue;
            }


            safeArgs.add(
                    arg
            );
        }


        try {


            JsonNode tree =
                    objectMapper.valueToTree(
                            safeArgs
                    );


            /**
             * 敏感字段脱敏。
             */
            maskSensitiveFields(
                    tree
            );


            return limit(

                    objectMapper.writeValueAsString(
                            tree
                    ),

                    2000

            );


        }
        catch (Exception exception) {


            return limit(

                    safeArgs.toString(),

                    2000

            );
        }
    }


    /**
     * =====================================================
     * 敏感字段脱敏
     * =====================================================
     */
    private void maskSensitiveFields(
            JsonNode node
    ) {


        if (node == null) {

            return;
        }


        /**
         * 对象
         */
        if (node.isObject()) {


            ObjectNode objectNode =
                    (ObjectNode) node;


            /**
             * 先把字段复制出来。
             *
             * 防止遍历过程中修改对象导致异常。
             */
            List<String> fieldNames =
                    new ArrayList<>();


            objectNode.fieldNames()
                    .forEachRemaining(
                            fieldNames::add
                    );


            for (
                    String fieldName :
                    fieldNames
            ) {


                String key =
                        fieldName
                                .toLowerCase(
                                        Locale.ROOT
                                );


                /**
                 * 敏感字段。
                 */
                if (
                        key.contains(
                                "password"
                        )
                                ||
                                key.contains(
                                        "token"
                                )
                                ||
                                key.contains(
                                        "secret"
                                )
                ) {


                    objectNode.put(

                            fieldName,

                            "***"

                    );


                }
                else {


                    maskSensitiveFields(

                            objectNode.get(
                                    fieldName
                            )
                    );
                }
            }


            return;
        }


        /**
         * 数组
         */
        if (node.isArray()) {


            for (
                    JsonNode child :
                    node
            ) {


                maskSensitiveFields(
                        child
                );
            }
        }
    }


    /**
     * =====================================================
     * 获取真实客户端IP
     * =====================================================
     */
    private String getClientIp(
            HttpServletRequest request
    ) {


        /**
         * Nginx代理。
         */
        String ip =
                request.getHeader(
                        "X-Forwarded-For"
                );


        if (
                ip != null
                        &&
                        !ip.isBlank()
                        &&
                        !"unknown".equalsIgnoreCase(
                                ip
                        )
        ) {


            /**
             * 多层代理：
             *
             * client, proxy1, proxy2
             *
             * 取第一个。
             */
            int comma =
                    ip.indexOf(
                            ','
                    );


            return comma > 0

                    ? ip.substring(
                    0,
                    comma
            ).trim()

                    : ip.trim();
        }


        /**
         * X-Real-IP
         */
        ip =
                request.getHeader(
                        "X-Real-IP"
                );


        if (
                ip != null
                        &&
                        !ip.isBlank()
        ) {


            return ip;
        }


        /**
         * 本地开发时：
         *
         * 可能为：
         *
         * 127.0.0.1
         *
         * 或：
         *
         * 0:0:0:0:0:0:0:1
         */
        return request.getRemoteAddr();
    }


    /**
     * =====================================================
     * 字符串最大长度
     * =====================================================
     */
    private String limit(

            String value,

            int maxLength

    ) {


        if (value == null) {

            return null;
        }


        if (
                value.length()
                        <=
                        maxLength
        ) {

            return value;
        }


        return value.substring(
                0,
                maxLength
        );
    }


}