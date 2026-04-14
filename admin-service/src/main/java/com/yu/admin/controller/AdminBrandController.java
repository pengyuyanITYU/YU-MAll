package com.yu.admin.controller;

import com.yu.admin.service.AdminBrandService;
import com.yu.api.dto.admin.AdminBrandDTO;
import com.yu.api.query.admin.AdminBrandPageQuery;
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
@RequestMapping("/admin/brands")
public class AdminBrandController {

    private final AdminBrandService adminBrandService;

    @PostMapping
    public AjaxResult<Void> add(@Valid @RequestBody AdminBrandDTO brandDTO) {
        return adminBrandService.add(brandDTO);
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return adminBrandService.delete(id);
    }

    @DeleteMapping("/batch")
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        return adminBrandService.deleteByIds(ids);
    }

    @PutMapping
    public AjaxResult<Void> update(@Valid @RequestBody AdminBrandDTO brandDTO) {
        return adminBrandService.update(brandDTO);
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return adminBrandService.getById(id);
    }

    @GetMapping
    public TableDataInfo list(AdminBrandPageQuery query) {
        return adminBrandService.list(query);
    }

    @GetMapping("/list")
    public AjaxResult<Object> simpleList() {
        return adminBrandService.simpleList();
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response) {
        adminBrandService.export(response);
    }
}
