package com.winforrd.taskflow.controller;


import com.winforrd.taskflow.common.result.Result;

import com.winforrd.taskflow.service.ExtensionCapabilityService;

import com.winforrd.taskflow.vo.ExtensionCapabilityVO;


import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


import java.util.List;


/**
 * =========================================================
 * 项目扩展能力Controller
 * =========================================================
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectExtensionController {


    private final ExtensionCapabilityService
            extensionCapabilityService;


    /**
     * =====================================================
     * 获取项目当前可用扩展能力
     * =====================================================
     *
     * GET
     *
     * /api/projects/{projectId}/extensions/capabilities
     */
    @GetMapping("/{projectId}/extensions/capabilities")
    public Result<List<ExtensionCapabilityVO>>
    getCapabilities(

            @PathVariable
            Long projectId

    ) {


        return Result.success(

                extensionCapabilityService
                        .getCapabilities(
                                projectId
                        )

        );
    }


}