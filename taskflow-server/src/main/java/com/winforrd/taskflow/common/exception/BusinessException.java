package com.winforrd.taskflow.common.exception;

import com.winforrd.taskflow.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(
            Integer code,
            String message
    ) {

        super(message);
        this.code = code;
    }

    public BusinessException(
            String message
    ) {

        super(message);
        this.code = 400;
    }

    public BusinessException(
            ErrorCode errorCode
    ) {

        super(
                errorCode.getMessage()
        );

        this.code =
                errorCode.getCode();
    }
}