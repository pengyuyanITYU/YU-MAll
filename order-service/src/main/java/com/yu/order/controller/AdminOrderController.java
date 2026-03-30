package com.yu.order.controller;

import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.UnauthorizedException;
import com.yu.common.utils.AdministratorContext;
import com.yu.common.utils.ExcelUtils;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.query.OrderPageQuery;
import com.yu.order.domain.vo.OrderDashBoardVO;
import com.yu.order.domain.vo.OrderVO;
import com.yu.order.service.IAdminOrderService;
import com.yu.order.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "admin order service")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final IOrderService orderService;

    private final IAdminOrderService adminOrderService;

    @ModelAttribute
    public void checkAdminLogin() {
        if (AdministratorContext.getUser() == null) {
            throw new UnauthorizedException("unauthorized");
        }
    }

    @ApiOperation("query order list")
    @GetMapping("/list")
    public TableDataInfo list(OrderPageQuery query) {
        return adminOrderService.listPage(query);
    }

    @ApiOperation("query order detail")
    @GetMapping("/{id}")
    @ApiImplicitParam(name = "id", value = "order id")
    public AjaxResult<OrderVO> getByOrderId(@PathVariable Long id) {
        return AjaxResult.success(adminOrderService.getByOrderId(id));
    }

    @ApiOperation("delete order")
    @DeleteMapping("/{id}")
    public AjaxResult<Void> deleteById(@PathVariable Long id) {
        orderService.deleteByOrderId(id);
        return AjaxResult.success();
    }

    @ApiOperation("batch delete order")
    @ApiImplicitParam(name = "ids", value = "order id list")
    @DeleteMapping
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        orderService.deleteByOrderIds(ids);
        return AjaxResult.success();
    }

    @ApiOperation("latest 5 orders")
    @GetMapping("/recent")
    public AjaxResult<List<OrderDashBoardVO>> recentOrders() {
        return AjaxResult.success(adminOrderService.recentOrders());
    }

    @ApiOperation("export excel")
    @PostMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        List<Order> list = orderService.list();
        ExcelUtils.exportEasyExcel(response, list, Order.class, "order list");
    }
}
