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
import org.springframework.cache.annotation.Caching;
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

    // ================== 增删改：必须清除缓存 ==================

    /**
     * 新增：这就意味着列表变了，必须清空所有 category 下的缓存（包括分页和详情）
     * 否则列表里查不到新数据
     */
    @PostMapping
    @ApiOperation("新增商品分类")
    // allEntries = true 表示：把 "category" 柜子里的所有数据全扔了，强制下次查库
    @CacheEvict(cacheNames = "category", allEntries = true)
    public AjaxResult<Void> addCategory(@Validated @RequestBody CategoryDTO categoryDTO) {
        log.info("新增商品分类：{}", categoryDTO.getName());
        boolean result = categoryService.addCategory(categoryDTO);
        return result ? AjaxResult.success() : AjaxResult.error("新增分类失败");
    }

    /**
     * 删除：同理，列表变了，详情也没了，简单粗暴清空所有
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除商品分类")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public AjaxResult<Void> deleteCategory(@PathVariable Long id) {
        log.info("删除商品分类：{}", id);
        boolean result = categoryService.deleteCategory(id);
        return result ? AjaxResult.success() : AjaxResult.error("删除分类失败");
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除商品分类")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public AjaxResult<Void> deleteCategories(@RequestParam List<Long> ids) {
        log.info("批量删除商品分类：{}", ids);
        return AjaxResult.toAjax(categoryService.deleteByIds(ids));
    }

    /**
     * 更新：
     * 1. 这个 ID 的详情变了 -> 要删
     * 2. 列表里显示的名称可能变了 -> 列表也要删
     * 所以还是 allEntries = true 最稳妥
     */
    @PutMapping
    @ApiOperation("更新商品分类")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public AjaxResult<Void> updateCategory(@Validated @RequestBody CategoryDTO categoryDTO) {
        log.info("更新商品分类：{}", categoryDTO.getId());
        boolean result = categoryService.updateCategory(categoryDTO);
        return result ? AjaxResult.success() : AjaxResult.error("更新分类失败");
    }

    // ================== 查询：开启缓存 ==================

    /**
     * ID 查询：Key 格式 -> category::id:101
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询商品分类")
    @Cacheable(cacheNames = "category", key = "'id:' + #id")
    public AjaxResult<CategoryVO> getCategoryById(@PathVariable Long id) {
        log.info("查询商品分类详情，ID：{}", id);
        CategoryVO categoryVO = categoryService.getCategoryById(id);
        return AjaxResult.success(categoryVO);
    }

    /**
     * 分页查询：Key 格式 -> category::page:1:10:手机
     */
    @GetMapping
    @ApiOperation("分页查询商品分类")
    public TableDataInfo getAllCategories(CategoryQuery query) {
        log.info("查询分类分页列表");
        return categoryService.getAllCategories(query);
    }

    /**
     * 全量查询：Key 格式 -> category::list
     */
    @GetMapping("/list")
    @ApiOperation("查询所有商品分类列表")
    @Cacheable(cacheNames = "category", key = "'list'")
    public AjaxResult<List<Category>> getAllCategories() {
        log.info("查询所有分类全量列表");
        return AjaxResult.success(categoryService.list());
    }

    @ApiOperation("导出Excel表")
    @PostMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        log.info("开始进行订单导出");
        List<Category> list1 = categoryService.list();
        ExcelUtils.exportEasyExcel(response, list1, Category.class, "商品类别列表");
    }
}