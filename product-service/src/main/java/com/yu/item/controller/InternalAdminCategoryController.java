package com.yu.item.controller;

import com.yu.api.dto.admin.AdminCategoryDTO;
import com.yu.api.query.admin.AdminCategoryPageQuery;
import com.yu.api.vo.admin.AdminCategoryExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.CategoryDTO;
import com.yu.item.domain.po.Category;
import com.yu.item.domain.query.CategoryQuery;
import com.yu.item.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/categories")
public class InternalAdminCategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    public AjaxResult<Void> add(@Validated @RequestBody AdminCategoryDTO categoryDTO) {
        return AjaxResult.toAjax(categoryService.addCategory(toCategoryDTO(categoryDTO)));
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return AjaxResult.toAjax(categoryService.deleteCategory(id));
    }

    @DeleteMapping("/batch")
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        return AjaxResult.toAjax(categoryService.deleteByIds(ids));
    }

    @PutMapping
    public AjaxResult<Void> update(@Validated @RequestBody AdminCategoryDTO categoryDTO) {
        return AjaxResult.toAjax(categoryService.updateCategory(toCategoryDTO(categoryDTO)));
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return AjaxResult.success(categoryService.getCategoryById(id));
    }

    @GetMapping
    public TableDataInfo list(AdminCategoryPageQuery query) {
        CategoryQuery target = new CategoryQuery();
        if (query != null) {
            BeanUtils.copyProperties(query, target);
        }
        return categoryService.getAllCategories(target);
    }

    @GetMapping("/simple")
    public AjaxResult<Object> simpleList() {
        return AjaxResult.success(categoryService.list());
    }

    @GetMapping("/export-data")
    public AjaxResult<List<AdminCategoryExportVO>> exportData() {
        return AjaxResult.success(categoryService.list().stream().map(this::toExportVO).collect(Collectors.toList()));
    }

    private CategoryDTO toCategoryDTO(AdminCategoryDTO source) {
        CategoryDTO target = new CategoryDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    private AdminCategoryExportVO toExportVO(Category category) {
        AdminCategoryExportVO exportVO = new AdminCategoryExportVO();
        BeanUtils.copyProperties(category, exportVO);
        return exportVO;
    }
}
