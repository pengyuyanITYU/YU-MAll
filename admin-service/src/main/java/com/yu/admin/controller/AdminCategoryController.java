package com.yu.admin.controller;

import com.yu.admin.service.AdminCategoryService;
import com.yu.api.dto.admin.AdminCategoryDTO;
import com.yu.api.query.admin.AdminCategoryPageQuery;
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
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @PostMapping
    public AjaxResult<Void> add(@Valid @RequestBody AdminCategoryDTO categoryDTO) {
        return adminCategoryService.add(categoryDTO);
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return adminCategoryService.delete(id);
    }

    @DeleteMapping("/batch")
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        return adminCategoryService.deleteByIds(ids);
    }

    @PutMapping
    public AjaxResult<Void> update(@Valid @RequestBody AdminCategoryDTO categoryDTO) {
        return adminCategoryService.update(categoryDTO);
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return adminCategoryService.getById(id);
    }

    @GetMapping
    public TableDataInfo list(AdminCategoryPageQuery query) {
        return adminCategoryService.list(query);
    }

    @GetMapping("/list")
    public AjaxResult<Object> simpleList() {
        return adminCategoryService.simpleList();
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response) {
        adminCategoryService.export(response);
    }
}
