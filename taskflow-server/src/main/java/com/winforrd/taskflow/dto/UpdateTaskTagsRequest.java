package com.winforrd.taskflow.dto;


import lombok.Data;


import java.util.ArrayList;
import java.util.List;


@Data
public class UpdateTaskTagsRequest {


    /**
     * 任务最终应该绑定的完整标签ID集合。
     *
     * 空数组表示清空标签。
     */
    private List<Long> tagIds = new ArrayList<>();


}