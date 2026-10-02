package com.winforrd.taskflow.service;


import com.winforrd.taskflow.vo.ExtensionCapabilityVO;


import java.util.List;


/**
 * =========================================================
 * 扩展能力查询服务
 * =========================================================
 */
public interface ExtensionCapabilityService {


    /**
     * 获取当前系统全部扩展能力。
     */
    List<ExtensionCapabilityVO> getCapabilities(Long projectId);


}