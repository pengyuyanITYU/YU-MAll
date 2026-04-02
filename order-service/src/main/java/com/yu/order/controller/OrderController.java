package com.yu.order.controller;

import com.yu.common.domain.AjaxResult;
import com.yu.order.domain.dto.CommentDTO;
import com.yu.order.domain.dto.OrderFormDTO;
import com.yu.order.domain.dto.UpdateOrderStatusDTO;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.vo.OrderVO;
import com.yu.order.service.IOrderDetailService;
import com.yu.order.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "订单管理")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final IOrderService orderService;
    private final IOrderDetailService orderDetailService;

    @ApiOperation("查询用户订单列表")
    @GetMapping("/{id}/list")
    public AjaxResult<List<OrderVO>> listById(@PathVariable Long id) {
        log.info("query order list by userId={}", id);
        List<OrderVO> orders = orderService.listById(id);
        return AjaxResult.success(orders);
    }

    @ApiOperation("查询订单详情")
    @GetMapping("/{id}")
    @ApiImplicitParam(name = "id", value = "订单ID")
    public AjaxResult<OrderVO> getByOrderId(@PathVariable Long id) {
        log.info("query order detail by orderId={}", id);
        OrderVO orderVO = orderService.getByOrderId(id);
        return AjaxResult.success(orderVO);
    }

    @ApiOperation("查询订单基础信息")
    @GetMapping("/getById/{id}")
    public AjaxResult<Order> getById(@PathVariable Long id) {
        log.info("query order by id={}", id);
        Order order = orderService.getById(id);
        return AjaxResult.success(order);
    }

    @ApiOperation("删除订单")
    @DeleteMapping("/{id}")
    public AjaxResult<Void> deleteById(@PathVariable Long id) {
        log.info("delete order by id={}", id);
        orderService.deleteByOrderId(id);
        return AjaxResult.success();
    }

    @ApiOperation("批量删除订单")
    @ApiImplicitParam(name = "ids", value = "订单ID集合")
    @DeleteMapping
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        orderService.deleteByOrderIds(ids);
        return AjaxResult.success();
    }

    @ApiOperation("新增订单")
    @PostMapping
    public AjaxResult<Void> addOrder(@RequestBody @Valid OrderFormDTO orderFormDTO) {
        log.info("create order request={}", orderFormDTO);
        return AjaxResult.toAjax(orderService.addOrder(orderFormDTO));
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public AjaxResult<Void> cancelOrder(@PathVariable Long id) {
        log.info("cancel order id={}", id);
        return AjaxResult.toAjax(orderService.cancelOrder(id));
    }

    @ApiOperation("确认收货")
    @PutMapping("/confirm/{id}")
    public AjaxResult<Void> confirmOrder(@PathVariable Long id) {
        log.info("confirm order id={}", id);
        return AjaxResult.toAjax(orderService.confirmOrder(id));
    }

    @ApiOperation("更新订单状态")
    @PutMapping({"/updateStatus", "/updateStatus/"})
    public AjaxResult<Void> updateOrderStatus(@RequestBody UpdateOrderStatusDTO updateOrderStatusDTO) {
        log.info("update order status req={}", updateOrderStatusDTO);
        return AjaxResult.toAjax(orderService.updateOrderStatus(updateOrderStatusDTO));
    }

    @ApiOperation("更新订单评价状态")
    @PutMapping("/commented")
    public AjaxResult<Void> updateOrderCommented(@RequestBody CommentDTO commentDTO) {
        log.info("update order commented req={}", commentDTO);
        orderDetailService.updateOrderCommented(commentDTO);
        return AjaxResult.success();
    }
}
