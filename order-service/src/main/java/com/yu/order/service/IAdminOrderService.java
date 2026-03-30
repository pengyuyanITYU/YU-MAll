package com.yu.order.service;

import com.yu.common.domain.page.TableDataInfo;
import com.yu.order.domain.query.OrderPageQuery;
import com.yu.order.domain.vo.OrderDashBoardVO;
import com.yu.order.domain.vo.OrderVO;

import java.util.List;

public interface IAdminOrderService {
    TableDataInfo listPage(OrderPageQuery query);

    OrderVO getByOrderId(Long id);

    List<OrderDashBoardVO> recentOrders();
}
