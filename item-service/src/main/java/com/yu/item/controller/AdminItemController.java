package com.yu.item.controller;

import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.UnauthorizedException;
import com.yu.common.utils.AdministratorContext;
import com.yu.common.utils.ExcelUtils;
import com.yu.item.domain.dto.ItemDTO;
import com.yu.item.domain.po.Item;
import com.yu.item.domain.query.ItemPageQuery;
import com.yu.item.domain.vo.ItemDashboardVO;
import com.yu.item.domain.vo.ItemDetailVO;
import com.yu.item.service.IItemService;
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
@RequestMapping("/admin/items")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "商品管理")
public class AdminItemController {

    private final IItemService itemService;

    @ModelAttribute
    public void checkAdminLogin() {
        if (AdministratorContext.getUser() == null) {
            throw new UnauthorizedException("unauthorized");
        }
    }

    @GetMapping("/list")
    @ApiOperation("分页查询商品")
    public TableDataInfo list(ItemPageQuery itemPageQuery) {
        log.info("查询管理端商品列表: {}", itemPageQuery);
        return itemService.listItem(itemPageQuery);
    }

    @GetMapping("/{id}")
    @ApiOperation("查询商品详情")
    @Cacheable(cacheNames = "item", key = "'id:' + #id")
    public AjaxResult<ItemDetailVO> getItemById(@PathVariable Long id) {
        return AjaxResult.success(itemService.getItemById(id));
    }

    @GetMapping("/batch")
    @ApiOperation("批量查询商品详情")
    @Cacheable(cacheNames = "item", key = "'list:' + #ids")
    public AjaxResult<List<ItemDetailVO>> getItemByIds(@RequestParam("ids") List<Long> ids) {
        return AjaxResult.success(itemService.getItemByIds(ids));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除商品")
    @CacheEvict(cacheNames = "item", allEntries = true)
    public AjaxResult<Void> deleteById(@PathVariable Long id) {
        return AjaxResult.toAjax(itemService.removeById(id));
    }

    @DeleteMapping
    @ApiOperation("批量删除商品")
    @CacheEvict(cacheNames = "item", allEntries = true)
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        return AjaxResult.toAjax(itemService.removeByIds(ids));
    }

    @PostMapping
    @ApiOperation("新增商品")
    @CacheEvict(cacheNames = "item", allEntries = true)
    public AjaxResult<Void> add(@Validated @RequestBody ItemDTO itemDTO) {
        itemService.add(itemDTO);
        return AjaxResult.success();
    }

    @PutMapping
    @ApiOperation("修改商品")
    @CacheEvict(cacheNames = "item", allEntries = true)
    public AjaxResult<Void> update(@Validated @RequestBody ItemDTO itemDTO) {
        itemService.updateItemById(itemDTO);
        return AjaxResult.success();
    }

    @GetMapping("/dashboard")
    @ApiOperation("查询商品管理看板")
    public AjaxResult<ItemDashboardVO> getDashboardData() {
        return AjaxResult.success(itemService.getItemDashboardData());
    }

    @PostMapping("/export")
    @ApiOperation("导出商品")
    public void exportExcel(HttpServletResponse response) {
        List<Item> list = itemService.list();
        ExcelUtils.exportEasyExcel(response, list, Item.class, "商品列表");
    }
}
