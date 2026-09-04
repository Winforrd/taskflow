package com.winforrd.taskflow.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


import lombok.Data;


@Data
public class CreateTagRequest {


    @NotBlank(
            message = "标签名称不能为空"
    )
    @Size(
            max = 50,
            message = "标签名称不能超过50个字符"
    )
    private String name;


    /**
     * 前端传：
     *
     * #409EFF
     */
    private String color;


}