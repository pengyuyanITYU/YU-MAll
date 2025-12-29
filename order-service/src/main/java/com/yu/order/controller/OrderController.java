package com.yu.order.controller;

import cn.hutool.core.bean.BeanUtil;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.query.OrderPageQuery;
import com.yu.order.domain.vo.OrderDashBoardVO;
import com.yu.order.domain.vo.OrderVO;
import com.yu.order.service.IOrderDetailService;
import com.yu.order.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "订单服务")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class OrderController {

    private final IOrderService orderService;

    private final IOrderDetailService orderDetailService;

    @ApiOperation("查询订单列表")
    @GetMapping("/list")
    public TableDataInfo listById(OrderPageQuery  query) {
        log.info("开始进行列表查询");
        return orderService.listPage(query);
    }

    @ApiOperation("查询订单详情")
    @GetMapping("/{id}")
    @Cacheable(cacheNames = "order",key = " 'id:'+ #id")
    @ApiImplicitParam(name = "id", value = "订单id")
    public AjaxResult<OrderVO> getByOrderId(@PathVariable Long id) {
        log.info("开始进行订单详情查询");
        OrderVO orderVO = orderService.getByOrderId(id);
        return AjaxResult.success(orderVO);
    }


    @ApiOperation("删除订单")
    @DeleteMapping("/{id}")
    @CacheEvict(cacheNames = "order",allEntries = true)
    public AjaxResult deleteById(@PathVariable Long id) {
        log.info("开始进行订单删除");
        orderService.deleteByOrderId(id);
        return AjaxResult.success();
    }

    @ApiOperation("批量删除订单")
    @ApiImplicitParam(name = "ids", value = "订单条目id集合")
    @DeleteMapping
    @CacheEvict(cacheNames = "order",allEntries = true)
    public AjaxResult deleteByIds(@RequestParam("ids") List<Long> ids){
        orderService.deleteByOrderIds(ids);
        return AjaxResult.success();
    }

//    @ApiOperation("更改订单为已发货")
//    @PutMapping("/consign/{id}")
//    public AjaxResult<Void> updateOrder(@PathVariable Long id){
//        log.info("更改订单评价状态为已评价");
//        orderService.consign(id);
//        return AjaxResult.success();
//    }

    @ApiOperation("最近5个订单")
    @GetMapping("/recent")
    public AjaxResult<List<OrderDashBoardVO>> recentOrders(){
        log.info("查询最近5个订单");
        List<OrderDashBoardVO> recentOrders = orderService.recentOrders();
        return AjaxResult.success(recentOrders);
    }

    @ApiOperation("导出Excel表")
    @PostMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        log.info("开始进行订单导出");
        List<Order> list1 = orderService.list();
        ExcelUtils.exportEasyExcel(response, list1, Order.class, "订单列表");
    }




}
