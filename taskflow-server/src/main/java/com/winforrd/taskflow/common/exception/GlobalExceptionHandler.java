package com.winforrd.taskflow.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.winforrd.taskflow.common.result.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLogin(NotLoginException e) {

        return new Result<>(
                401,
                "登录状态已失效，请重新登录",
                null
        );
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {

        return new Result<>(
                e.getCode(),
                e.getMessage(),
                null
        );
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {

        String message = e.getBindingResult()
                        .getFieldErrors()
                        .get(0)
                        .getDefaultMessage();

        return new Result<>(
                400,
                message,
                null
        );
    }

    /**
     * 其他运行时异常
     *
     * 理论上属于程序异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {

        e.printStackTrace();

        return new Result<>(
                500,
                "系统运行异常，请稍后重试",
                null
        );
    }

    /**
     * 未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {

        e.printStackTrace();

        return new Result<>(
                500,
                "系统异常，请稍后重试",
                null
        );
    }
}