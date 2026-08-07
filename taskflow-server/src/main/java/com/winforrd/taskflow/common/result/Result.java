package com.winforrd.taskflow.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<Void> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<Void> error(int code ,String message) {
        return new Result<>(code, message, null);
    }
}
