package com.yu.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.order.domain.dto.CommentDTO;
import com.yu.order.domain.dto.OrderFormDTO;
import com.yu.order.domain.enums.OrderStatus;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.query.OrderPageQuery;
import com.yu.order.domain.vo.OrderDashBoardVO;
import com.yu.order.domain.vo.OrderVO;


import java.util.List;

public interface IOrderService extends IService<Order> {

    TableDataInfo listPage(OrderPageQuery orderPageQuery    );


    void deleteByOrderId(Long id);

    void deleteByOrderIds(List<Long> ids);

    OrderVO getByOrderId(Long id);

    void batchConsignOrders();

    List<OrderDashBoardVO> recentOrders();
}
