package com.yu.sale.controller;

import com.yu.common.domain.AjaxResult;
import com.yu.sale.domain.vo.SaleDashboardVO;
import com.yu.sale.service.IPayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "销量统计接口")
@RestController
@RequestMapping("/admin/sales")
@RequiredArgsConstructor
@Slf4j
public class SaleController {

    private final IPayOrderService payOrderService;

    @ApiOperation("获取销量看板统计数据")
    @GetMapping("/dashboard")
    public AjaxResult<SaleDashboardVO> getSaleDashboard() {
        log.info("开始查询销量看板数据");
        SaleDashboardVO dashboardVO = payOrderService.getSaleDashboardData();
        return AjaxResult.success(dashboardVO);
    }
    
    // 保留原有的 id 查询接口 (修复了之前的编译错误)
    @ApiOperation("查询支付订单详情")
    @GetMapping("/pay-orders/{id}")
    public AjaxResult getPayOrderById(@PathVariable Long id) {
        return AjaxResult.success(payOrderService.getById(id));
    }
}