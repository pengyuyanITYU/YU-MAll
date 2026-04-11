package com.yu.order.controller;

import com.yu.api.query.admin.AdminOrderPageQuery;
import com.yu.api.vo.admin.AdminOrderExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.query.OrderPageQuery;
import com.yu.order.service.IAdminOrderService;
import com.yu.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/orders")
public class InternalAdminOrderController {

    private final IOrderService orderService;
    private final IAdminOrderService adminOrderService;

    @GetMapping("/list")
    public TableDataInfo list(AdminOrderPageQuery query) {
        OrderPageQuery target = new OrderPageQuery();
        if (query != null) {
            BeanUtils.copyProperties(query, target);
        }
        return adminOrderService.listPage(target);
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return AjaxResult.success(adminOrderService.getByOrderId(id));
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        orderService.deleteByOrderId(id);
        return AjaxResult.success();
    }

    @DeleteMapping
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        orderService.deleteByOrderIds(ids);
        return AjaxResult.success();
    }

    @GetMapping("/recent")
    public AjaxResult<Object> recent() {
        return AjaxResult.success(adminOrderService.recentOrders());
    }

    @GetMapping("/export-data")
    public AjaxResult<List<AdminOrderExportVO>> exportData() {
        return AjaxResult.success(orderService.list().stream().map(this::toExportVO).collect(Collectors.toList()));
    }

    private AdminOrderExportVO toExportVO(Order order) {
        AdminOrderExportVO exportVO = new AdminOrderExportVO();
        BeanUtils.copyProperties(order, exportVO);
        return exportVO;
    }
}
