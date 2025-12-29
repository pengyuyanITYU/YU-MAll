package com.yu.dashBoard.service.impl;

import com.yu.api.client.ItemClient;
import com.yu.api.client.OrderClient;
import com.yu.api.client.PayClient;
import com.yu.api.vo.ItemDashboardVO;
import com.yu.api.vo.OrderDashBoardVO;
import com.yu.api.vo.SaleDashboardVO;
import com.yu.common.domain.AjaxResult;
import com.yu.dashBoard.domain.vo.DashboardVO;
import com.yu.dashBoard.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashBoardServiceImpl implements IDashboardService {

    private final PayClient payClient;
    private final ItemClient itemClient;
    private final OrderClient orderClient;
    @Override
    public DashboardVO getDashboardData() {
        AjaxResult<SaleDashboardVO> saleDashboard = payClient.getSaleDashboard();
        if (!saleDashboard.isSuccess()) {
            log.error("获取销量看板数据失败");
            throw new RuntimeException("获取销量看板数据失败");
        }
        SaleDashboardVO saleDashboardVO = saleDashboard.getData();
        AjaxResult<ItemDashboardVO> dashboardData = itemClient.getDashboardData();
        if (!dashboardData.isSuccess()) {
            log.error("获取商品看板数据失败");
            throw new RuntimeException("获取商品看板数据失败");
        }
        ItemDashboardVO itemDashboardVO = dashboardData.getData();
        AjaxResult<List<OrderDashBoardVO>> listAjaxResult = orderClient.recentOrders();
        if (!listAjaxResult.isSuccess()){
            log.error("获取订单看板数据失败");
            throw new RuntimeException("获取订单看板数据失败");
        }
        List<OrderDashBoardVO> orderDashBoardVOList = listAjaxResult.getData();
        DashboardVO build = DashboardVO.builder().itemDashboard(itemDashboardVO)
                .saleDashboard(saleDashboardVO)
                .recentOrders(orderDashBoardVOList)
                .build();
        return build;
    }
}
