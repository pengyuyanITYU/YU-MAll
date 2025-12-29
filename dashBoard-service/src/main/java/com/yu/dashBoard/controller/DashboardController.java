package com.yu.dashBoard.controller;

import com.yu.common.domain.AjaxResult;
import com.yu.dashBoard.domain.vo.DashboardVO;
import com.yu.dashBoard.service.IDashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "管理端仪表盘")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final IDashboardService dashboardService;

    @ApiOperation("获取首页统计数据")
    @GetMapping
    public AjaxResult<DashboardVO> getDashboardData() {
        log.info("加载管理端首页数据...");
        DashboardVO vo = dashboardService.getDashboardData();
        return AjaxResult.success(vo);
    }
}