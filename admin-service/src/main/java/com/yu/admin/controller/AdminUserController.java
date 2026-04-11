package com.yu.admin.controller;

import com.yu.admin.service.AdminUserService;
import com.yu.api.query.admin.AdminUserPageQuery;
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
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public TableDataInfo list(AdminUserPageQuery query) {
        return adminUserService.list(query);
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return adminUserService.getById(id);
    }

    @GetMapping("/all")
    public AjaxResult<?> getByIds(@RequestParam List<Long> ids) {
        return adminUserService.getByIds(ids);
    }

    @PutMapping("/{id}/{status}")
    public AjaxResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return adminUserService.updateStatus(id, status);
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response) {
        adminUserService.export(response);
    }
}
