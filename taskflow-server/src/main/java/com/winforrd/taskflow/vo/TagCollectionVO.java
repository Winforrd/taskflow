package com.winforrd.taskflow.vo;


import lombok.Data;


import java.util.ArrayList;
import java.util.List;


@Data
public class TagCollectionVO {


    /**
     * 当前用户是否可以创建 / 修改 / 删除
     * 当前作用域标签。
     */
    private Boolean canManage;


    private List<TagVO> tags =
            new ArrayList<>();


}