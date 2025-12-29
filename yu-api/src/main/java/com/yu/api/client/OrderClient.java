package com.yu.api.client;

import com.yu.api.dto.CommentDTO;
import com.yu.api.enums.OrderStatus;
import com.yu.api.fallbacks.OrderFallbackFactory;
import com.yu.api.po.Order;
import com.yu.api.vo.OrderDashBoardVO;
import com.yu.common.domain.AjaxResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name="yu-mall-order-service",path="/admin/orders",fallbackFactory = OrderFallbackFactory.class)
public interface OrderClient {

    @PutMapping("/updateStatus/{id}")
    AjaxResult<Void> updateOrderStatus(@PathVariable Long id, @RequestParam("status") OrderStatus status);

    @GetMapping("/recent")
    AjaxResult<List<OrderDashBoardVO>> recentOrders();
}
