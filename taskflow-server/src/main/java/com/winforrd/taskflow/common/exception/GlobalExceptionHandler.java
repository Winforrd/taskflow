package com.winforrd.taskflow.common.exception;

import cn.dev33.satoken.exception.SaTokenException;
import com.winforrd.taskflow.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 业务异常
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handle(Exception e) {
        // 记录日志
        log.warn("业务异常：{}",e.getMessage());
        // 返回结果
        return Result.error(401,e.getMessage());
    }

    // 权限异常
    @ExceptionHandler(SaTokenException.class)
    public Result<?> handle(SaTokenException e) {
        // 记录日志
        log.warn("权限异常：{}",e.getMessage());
        // 返回结果
        return Result.error(401,"登录已过期，请重新登录");
    }

}
