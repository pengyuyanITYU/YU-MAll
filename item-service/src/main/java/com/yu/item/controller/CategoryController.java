package com.yu.item.controller;

import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import com.yu.item.domain.dto.CategoryDTO;
import com.yu.item.domain.po.Category;
import com.yu.item.domain.query.CategoryQuery;
import com.yu.item.domain.vo.CategoryVO;
import com.yu.item.service.ICategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@Api(tags = "商品分类管理")
public class CategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    @ApiOperation("新增分类")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public AjaxResult<Void> addCategory(@Validated @RequestBody CategoryDTO categoryDTO) {
        return AjaxResult.toAjax(categoryService.addCategory(categoryDTO));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除分类")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public AjaxResult<Void> deleteCategory(@PathVariable Long id) {
        return AjaxResult.toAjax(categoryService.deleteCategory(id));
    }

    @DeleteMapping("/batch")
    @ApiOperation("批量删除分类")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public AjaxResult<Void> deleteCategories(@RequestParam List<Long> ids) {
        return AjaxResult.toAjax(categoryService.deleteByIds(ids));
    }

    @PutMapping
    @ApiOperation("修改分类")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public AjaxResult<Void> updateCategory(@Validated @RequestBody CategoryDTO categoryDTO) {
        return AjaxResult.toAjax(categoryService.updateCategory(categoryDTO));
    }

    @GetMapping("/{id}")
    @ApiOperation("按ID查询分类")
    @Cacheable(cacheNames = "category", key = "'id:' + #id")
    public AjaxResult<CategoryVO> getCategoryById(@PathVariable Long id) {
        return AjaxResult.success(categoryService.getCategoryById(id));
    }

    @GetMapping
    @ApiOperation("分页查询分类")
    public TableDataInfo getAllCategories(CategoryQuery query) {
        return categoryService.getAllCategories(query);
    }

    @GetMapping("/list")
    @ApiOperation("查询分类列表")
    @Cacheable(cacheNames = "category", key = "'list'")
    public AjaxResult<List<Category>> getAllCategories() {
        return AjaxResult.success(categoryService.list());
    }

    @PostMapping("/export")
    @ApiOperation("导出分类")
    public void exportExcel(HttpServletResponse response) {
        ExcelUtils.exportEasyExcel(response, categoryService.list(), Category.class, "商品分类列表");
    }
}
