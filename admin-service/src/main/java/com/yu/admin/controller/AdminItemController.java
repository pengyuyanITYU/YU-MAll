package com.yu.admin.controller;

import com.yu.admin.service.AdminItemService;
import com.yu.api.dto.admin.AdminItemDTO;
import com.yu.api.query.admin.AdminItemPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/items")
public class AdminItemController {

    private final AdminItemService adminItemService;

    @GetMapping("/list")
    public TableDataInfo list(AdminItemPageQuery query) {
        return adminItemService.list(query);
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return adminItemService.getById(id);
    }

    @GetMapping("/batch")
    public AjaxResult<Object> getByIds(@RequestParam("ids") List<Long> ids) {
        return adminItemService.getByIds(ids);
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return adminItemService.delete(id);
    }

    @DeleteMapping
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        return adminItemService.deleteByIds(ids);
    }

    @PostMapping
    public AjaxResult<Void> add(@Valid @RequestBody AdminItemDTO itemDTO) {
        return adminItemService.add(itemDTO);
    }

    @PutMapping
    public AjaxResult<Void> update(@Valid @RequestBody AdminItemDTO itemDTO) {
        return adminItemService.update(itemDTO);
    }

    @GetMapping("/dashboard")
    public AjaxResult<Object> dashboard() {
        return adminItemService.dashboard();
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response) {
        adminItemService.export(response);
    }
}
