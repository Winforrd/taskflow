package com.winforrd.taskflow.aspect;


import cn.dev33.satoken.stp.StpUtil;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import com.winforrd.taskflow.entity.OperationLog;
import com.winforrd.taskflow.entity.Project;
import com.winforrd.taskflow.entity.SubTask;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.entity.Task;


import com.winforrd.taskflow.mapper.OperationLogMapper;
import com.winforrd.taskflow.mapper.ProjectMapper;
import com.winforrd.taskflow.mapper.SubTaskMapper;
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
 * 功能：
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
 * 特别处理：
 * 创建任务：
 * POST /api/projects/{projectId}/tasks
 * URL里没有taskId，
 * 所以创建成功后从Controller返回结果中取得新taskId。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    private final TaskMapper taskMapper;

    private final SubTaskMapper subTaskMapper;

    private final ProjectMapper projectMapper;

    private final SysUserMapper sysUserMapper;

    private final ObjectMapper objectMapper;

    /**
     * =====================================================
     * URI ID提取规则
     * =====================================================
     */

    private static final Pattern TEAM_PATTERN = Pattern.compile("/api/teams/(\\d+)");

    private static final Pattern PROJECT_PATTERN = Pattern.compile("/api/projects/(\\d+)");

    private static final Pattern TASK_PATTERN = Pattern.compile("/api/tasks/(\\d+)");

    private static final Pattern SUBTASK_PATTERN = Pattern.compile("/api/subtasks/(\\d+)");

    /**
     * 创建任务接口：
     * POST
     * /api/projects/{projectId}/tasks
     */
    private static final Pattern CREATE_TASK_PATTERN = Pattern.compile("^/api/projects/\\d+/tasks/?$");

    /**
     * =====================================================
     * 拦截Controller
     * =====================================================
     */
    @Around("execution(public * com.winforrd.taskflow.controller..*(..))")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求属性
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 非HTTP请求直接执行。
        if (attributes == null) {
            return joinPoint.proceed();
        }
        // 获取请求
        HttpServletRequest request = attributes.getRequest();
        // 获取HTTP方法
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        // 获取URI
        String uri = request.getRequestURI();
        // 查询类操作不记录操作日志, 只记录POST PUT DELETE
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return joinPoint.proceed();
        }

        //认证接口不记录
        // 防止登录密码等敏感参数进入操作日志。
        if (uri.startsWith("/api/auth")) {
            return joinPoint.proceed();
        }
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 执行目标方法
        Throwable businessError = null;
        // 保存Controller实际返回结果
        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            // 保存Controller实际返回结果
            return result;
        } catch (Throwable throwable) {
            businessError = throwable;
            throw throwable;
        } finally {
            // 记录结束时间
            long costMs = System.currentTimeMillis() - startTime;
            try {
                // 保存操作日志
                saveOperationLog(joinPoint, request, method, uri, costMs, businessError, result);
            }
            catch (Exception logException) {
                // 日志记录失败绝不能影响正常业务。
                log.error("保存操作日志失败", logException);
            }
        }
    }

    /**
     * =====================================================
     * 保存操作日志
     * =====================================================
     */
    private void saveOperationLog(ProceedingJoinPoint joinPoint, HttpServletRequest request, String method, String uri, long costMs, Throwable throwable, Object result) {
        // 新建日志对象, 准备保存
        OperationLog operationLog = new OperationLog();
        // 设置HTTP方法
        operationLog.setHttpMethod(method);
        // 设置请求URI
        operationLog.setRequestUri(limit(uri, 500));
        // 设置IP
        operationLog.setIp(getClientIp(request));
        // 设置用户代理
        operationLog.setUserAgent(limit(request.getHeader("User-Agent"), 500));
        // 设置执行耗时
        operationLog.setCostMs(costMs);
        // 设置创建时间
        operationLog.setCreatedTime(LocalDateTime.now());
        // 设置模块
        operationLog.setModule(resolveModule(uri));
        // 设置操作
        operationLog.setOperation(resolveOperation(method, uri));
        // 设置用户
        fillUser(operationLog);
        // 设置业务关联信息
        fillBusinessContext(operationLog, method, uri, result);
        // 设置请求参数
        operationLog.setRequestParams(serializeArguments(joinPoint.getArgs()));
        // 设置成功失败
        if (throwable == null) {
            // 设置成功
            operationLog.setSuccess(1);
        } else {
            // 设置失败
            operationLog.setSuccess(0);
            // 设置错误信息
            operationLog.setErrorMessage(limit(throwable.getMessage(), 1000));
        }
        // 保存日志
        operationLogMapper.insert(operationLog);
    }

    /**
     * =====================================================
     * 当前用户
     * =====================================================
     */
    private void fillUser(OperationLog logEntity) {
        // 如果未登录则不处理
        if (!StpUtil.isLogin()) {
            return;
        }
        // 获取用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        // 设置用户ID
        logEntity.setUserId(userId);
        // 设置用户名
        SysUser user = sysUserMapper.selectById(userId);
        // 如果用户不存在则不处理
        if (user == null) {
            return;
        }
        // 如果用户昵称不为空则使用昵称，为空则使用用户名
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            logEntity.setUsername(user.getNickname());
        } else {
            logEntity.setUsername(user.getUsername());
        }
    }
    /**
     * =====================================================
     * 填充业务关联信息
     * =====================================================
     * 普通任务操作：
     * /api/tasks/10/status
     * 可以直接从URI得到taskId=10。
     * 创建任务：
     * /api/projects/4/tasks
     * URI只能得到projectId=4。
     * 创建成功以后：
     * Controller：
     * Result<Long>
     * data = 新任务ID
     * 所以这里额外从result取得taskId。
     */
    private void fillBusinessContext(OperationLog logEntity, String method, String uri, Object result) {
        /**
         * =================================================
         * 第一步：
         * 先从URI获取ID。
         * =================================================
         */
        Long taskId = extractId(TASK_PATTERN, uri);
        Long projectId = extractId(PROJECT_PATTERN, uri);
        Long teamId = extractId(TEAM_PATTERN, uri);
        /**
         * =================================================
         * 第二步：
         * 创建任务属于特殊情况。
         * POST /api/projects/{projectId}/tasks
         * URL里面没有taskId。
         * Controller返回：
         * {
         *     code: ...,
         *     data: 新任务ID
         * }
         * 从返回值中取得taskId。
         * =================================================
         */
        if (taskId == null && "POST".equals(method) && CREATE_TASK_PATTERN.matcher(uri).matches()) {
            taskId = extractCreatedIdFromResult(result);
        }
        /**
         * =================================================
         * 子任务独立路径：
         * /api/subtasks/{id}
         * /api/subtasks/{id}/status
         * URL 中没有 taskId，需要反查子任务。
         * =================================================
         */
        if (taskId == null) {
            // 尝试从URI获取子任务ID
            Long subTaskId = extractId(SUBTASK_PATTERN, uri);
            // 如果从URI获取到子任务ID，则反查子任务的父任务ID
            if (subTaskId != null) {
                SubTask subTask = subTaskMapper.selectById(subTaskId);
                if (subTask != null) {
                    taskId = subTask.getTaskId();
                }
            }
        }
        /**
         * =================================================
         * 第三步：
         * 如果拿到了taskId，
         * 保存taskId并反查project。
         * =================================================
         */
        if (taskId != null) {
            // 设置任务ID
            logEntity.setTaskId(taskId);
            // 反查任务的项目ID
            Task task = taskMapper.selectById(taskId);
            if (task != null) {
                projectId = task.getProjectId();
            }
        }
        /**
         * =================================================
         * 第四步：
         * 项目ID。
         * 再通过项目找到团队ID。
         * =================================================
         */
        if (projectId != null) {
            // 设置项目ID
            logEntity.setProjectId(projectId);
            // 反查项目所属团队ID
            Project project = projectMapper.selectById(projectId);
            if (project != null) {
                teamId = project.getTeamId();
            }
        }
        // 设置团队ID
        if (teamId != null) {
            // 设置团队ID
            logEntity.setTeamId(teamId);
        }
    }
    /**
     * =====================================================
     * 从Controller返回结果获取新创建对象ID
     * =====================================================
     * 当前任务创建接口：
     * Result<Long>
     * 最终JSON结构类似：
     * {
     *     "code": 200,
     *     "message": "success",
     *     "data": 25
     * }
     * 为了不强依赖Result类内部getter名称，
     * 直接通过Jackson转换成JsonNode，
     * 然后读取data。
     */
    private Long extractCreatedIdFromResult(Object result) {
        // 如果结果为空则返回空
        if (result == null) {
            return null;
        }
        try {
            // 将结果转换成JsonNode
            JsonNode root = objectMapper.valueToTree(result);
            // 如果根节点为空或没有data字段则返回空
            if (root == null || !root.has("data")) {
                return null;
            }
            // 获取data字段
            JsonNode dataNode = root.get("data");
            if (dataNode == null || dataNode.isNull()) {
                return null;
            }
            // 如果data字段是整数则返回
            if (dataNode.isIntegralNumber()) {
                return dataNode.longValue();
            }
            // 如果data字段是文本则尝试转换成Long
            if (dataNode.isTextual()) {
                // 尝试将文本转换成Long
                String value = dataNode.asText();
                // 如果文本不为空且不为空白则转换成Long
                if (value != null && !value.isBlank()) {
                    return Long.valueOf(value);
                }
            }
        } catch (Exception exception) {
            log.debug("从Controller返回值解析新建任务ID失败", exception);
        }
        return null;
    }

    /**
     * =====================================================
     * 从URI提取ID
     * =====================================================
     */
    private Long extractId(Pattern pattern, String uri) {
        // 创建Matcher，用于匹配URI
        Matcher matcher = pattern.matcher(uri);
        // 如果没有找到匹配的ID则返回空
        if (!matcher.find()) {
            return null;
        }
        try {
            // 尝试将匹配到的字符串转换成Long
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
    /**
     * =====================================================
     * 模块识别
     * =====================================================
     */
    private String resolveModule(String uri) {
        // 任务
        if (uri.startsWith("/api/tasks/")) {
            // 任务评论
            if (uri.contains("/comments")) {
                return "COMMENT";
            }
            // 任务子任务
            if (uri.contains("/subtasks")) {
                return "SUB_TASK";
            }
            return "TASK";
        }
        // 子任务
        if (uri.startsWith("/api/subtasks/")) {
            return "SUB_TASK";
        }
        // 项目
        if (uri.startsWith("/api/projects/")) {
            // 项目任务
            if (uri.contains("/tasks")) {
                return "TASK";
            }
            // 项目成员
            if (uri.contains("/members")) {
                return "PROJECT";
            }
            return "PROJECT";
        }
        // 团队
        if (uri.startsWith("/api/teams/")) {
            return "TEAM";
        }
        // 通知
        if (uri.startsWith("/api/notifications")) {
            return "NOTIFICATION";
        }
        // 用户
        if (uri.startsWith("/api/users") || uri.startsWith("/api/profile")) {
            return "USER";
        }
        return "OTHER";
    }
    /**
     * =====================================================
     * 操作名称识别
     * =====================================================
     */
    private String resolveOperation(String method, String uri) {
        // 任务状态
        if (uri.matches("/api/tasks/\\d+/status/?")) {
            return "变更任务状态";
        }
        // 任务负责人
        if (uri.matches("/api/tasks/\\d+/assignees/\\d+/?")) {
            return "POST".equals(method) ? "添加任务负责人" : "移除任务负责人";
        }
        // 任务评论
        if (uri.contains("/comments")) {
            if ("POST".equals(method)) {
                return "发表评论";
            }
            if ("DELETE".equals(method)) {
                return "删除评论";
            }
        }
        // 子任务
        if (uri.contains("/subtasks")) {
            if (uri.matches("/api/tasks/\\d+/subtasks/\\d+/status/?")) {
                return "变更子任务状态";
            }
            if (uri.matches("/api/tasks/\\d+/subtasks/?") && "POST".equals(method)) {
                return "创建子任务";
            }
            if (uri.matches("/api/tasks/\\d+/subtasks/\\d+/?")) {
                return switch (method) {
                    case "PUT", "PATCH" -> "修改子任务";
                    case "DELETE" -> "删除子任务";
                    default -> "子任务操作";
                };
            }
            return "子任务操作";
        }
        // 创建任务
        if (CREATE_TASK_PATTERN.matcher(uri).matches() && "POST".equals(method)) {
            return "创建任务";
        }
        // 修改任务
        if (uri.matches("/api/tasks/\\d+/?") && ("PUT".equals(method) || "PATCH".equals(method))) {
            return "修改任务";
        }
        // 项目成员
        if (uri.contains("/members") && uri.startsWith("/api/projects/")) {
            return switch (method) {
                case "POST" -> "添加项目成员";
                case "PUT", "PATCH" -> "修改项目成员";
                case "DELETE" -> "移除项目成员";
                default -> "项目成员操作";
            };
        }
        // 通知
        if (uri.startsWith("/api/notifications")) {
            return uri.endsWith("/read-all") ? "全部通知已读" : "标记通知已读";
        }
        return switch (method) {
            case "POST" -> "新增数据";
            case "PUT", "PATCH" -> "修改数据";
            case "DELETE" -> "删除数据";
            default -> "数据操作";
        };
    }
    /**
     * =====================================================
     * 请求参数序列化
     * =====================================================
     */
    private String serializeArguments(Object[] args) {
        // 如果参数为空则返回空
        if (args == null || args.length == 0) {
            return null;
        }
        // 创建一个安全参数列表，用于存储非敏感参数
        List<Object> safeArgs = new ArrayList<>();
        for (Object arg : args) {
            // 如果参数为空则添加null并继续下一个参数
            if (arg == null) {
                safeArgs.add(null);
                continue;
            }
            // 如果参数是Servlet对象或MultipartFile则跳过
            if (arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof MultipartFile || arg instanceof MultipartFile[]) {
                continue;
            }
            // 添加参数到安全参数列表
            safeArgs.add(arg);
        }
        try {
            // 将安全参数列表转换成JsonNode
            JsonNode tree = objectMapper.valueToTree(safeArgs);
            // 对JsonNode进行敏感字段脱敏
            maskSensitiveFields(tree);
            // 将JsonNode转换成字符串并返回，最多2000个字符
            return limit(objectMapper.writeValueAsString(tree), 2000);
        }
        catch (Exception exception) {
            // 如果转换成Json字符串失败则返回参数的字符串表示，最多2000个字符
            return limit(safeArgs.toString(), 2000);
        }
    }
    /**
     * =====================================================
     * 敏感字段脱敏
     * =====================================================
     */
    private void maskSensitiveFields(JsonNode node) {
        // 如果节点为空则返回
        if (node == null) {
            return;
        }
        // 如果节点是对象则进行敏感字段脱敏
        if (node.isObject()) {
            // 获取对象节点
            ObjectNode objectNode = (ObjectNode) node;
            // 获取对象节点的字段名列表
            List<String> fieldNames = new ArrayList<>();
            // 遍历对象节点的字段名列表
            objectNode.fieldNames().forEachRemaining(fieldNames::add);
            // 遍历字段名列表
            for (String fieldName : fieldNames) {
                // 获取字段名的小写形式
                String key = fieldName.toLowerCase(Locale.ROOT);
                // 如果字段名包含密码、令牌或密钥则脱敏
                if (key.contains("password") || key.contains("token") || key.contains("secret")) {
                    objectNode.put(fieldName, "***");
                } else {
                    maskSensitiveFields(objectNode.get(fieldName));
                }
            }
            return;
        }
        // 如果节点是数组则进行敏感字段脱敏
        if (node.isArray()) {
            // 遍历数组节点的子节点
            for (JsonNode child : node) {
                // 对子节点进行敏感字段脱敏
                maskSensitiveFields(child);}
        }
    }
    /**
     * =====================================================
     * 获取真实客户端IP
     * =====================================================
     */
    private String getClientIp(HttpServletRequest request) {
        // 优先从X-Forwarded-For头获取客户端IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            // 多层代理时取第一个非unknown的IP
            int comma = ip.indexOf(',');
            // 如果有逗号则取第一个非unknown的IP
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        // 如果X-Forwarded-For头为空则从X-Real-IP头获取客户端IP
        ip = request.getHeader("X-Real-IP");
        // 如果X-Real-IP头为空则从远程地址获取客户端IP
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        // 如果X-Real-IP头为空则从远程地址获取客户端IP
        return request.getRemoteAddr();
    }
    /**
     * =====================================================
     * 字符串最大长度
     * =====================================================
     */
    private String limit(String value, int maxLength) {
        // 如果值为空则返回空
        if (value == null) {
            return null;
        }
        // 如果值长度小于等于最大长度则返回值
        if (value.length() <= maxLength) {
            return value;
        }
        // 如果值长度大于最大长度则返回截取后的值
        return value.substring(0, maxLength);
    }
}