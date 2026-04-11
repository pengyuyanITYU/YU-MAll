package com.yu.admin.controller;

import com.yu.admin.service.AdminOrderService;
import com.yu.api.query.admin.AdminOrderPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping("/list")
    public TableDataInfo list(AdminOrderPageQuery query) {
        return adminOrderService.list(query);
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return adminOrderService.getById(id);
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return adminOrderService.delete(id);
    }

    @DeleteMapping
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        return adminOrderService.deleteByIds(ids);
    }

    @GetMapping("/recent")
    public AjaxResult<Object> recent() {
        return adminOrderService.recent();
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response) {
        adminOrderService.export(response);
    }
}
