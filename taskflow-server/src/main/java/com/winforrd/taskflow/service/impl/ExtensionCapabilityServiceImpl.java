package com.winforrd.taskflow.service.impl;


import com.winforrd.taskflow.extension.ExtensionProvider;

import com.winforrd.taskflow.service.DataPermissionService;

import com.winforrd.taskflow.service.ExtensionCapabilityService;

import com.winforrd.taskflow.vo.ExtensionCapabilityVO;


import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;


import java.util.Comparator;

import java.util.List;


/**
 * =========================================================
 * 扩展能力服务
 * =========================================================
 *
 * Spring会自动把所有实现：
 *
 * ExtensionProvider
 *
 * 的Bean注入providers。
 *
 *
 * 当前：
 *
 * 4个DisabledProvider
 *
 *
 * 未来：
 *
 * 可以替换成真正的Provider。
 */
@Service
@RequiredArgsConstructor
public class ExtensionCapabilityServiceImpl
        implements ExtensionCapabilityService {


    /**
     * Spring自动收集所有扩展Provider。
     */
    private final List<ExtensionProvider>
            providers;


    /**
     * 复用上一阶段统一数据权限。
     */
    private final DataPermissionService
            dataPermissionService;


    @Override
    public List<ExtensionCapabilityVO>
    getCapabilities(
            Long projectId
    ) {


        /**
         * =================================================
         * 数据权限
         * =================================================
         *
         * 用户不能查看自己无权访问项目的扩展信息。
         */
        dataPermissionService.checkProjectView(
                projectId
        );


        return providers
                .stream()

                /**
                 * 保证接口返回顺序稳定。
                 */
                .sorted(
                        Comparator.comparing(
                                provider ->
                                        provider
                                                .getCode()
                                                .ordinal()
                        )
                )

                .map(
                        this::convert
                )

                .toList();
    }


    private ExtensionCapabilityVO convert(
            ExtensionProvider provider
    ) {


        ExtensionCapabilityVO vo =
                new ExtensionCapabilityVO();


        vo.setCode(
                provider
                        .getCode()
                        .name()
        );


        vo.setName(
                provider
                        .getDisplayName()
        );


        vo.setDescription(
                provider
                        .getDescription()
        );


        vo.setEnabled(
                provider
                        .isEnabled()
        );


        return vo;
    }


}