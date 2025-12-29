package com.yu.item.controller;

import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import com.yu.item.domain.dto.ItemDTO;
import com.yu.item.domain.po.Item;
import com.yu.item.domain.query.ItemPageQuery;
import com.yu.item.domain.vo.ItemDashboardVO;
import com.yu.item.domain.vo.ItemDetailVO;
import com.yu.item.service.IItemService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/admin/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final IItemService itemService;

    @GetMapping("/list")
    @ApiOperation("查询商品列表")
    public TableDataInfo list(ItemPageQuery itemPageQuery) {
        log.info("开始执行查询商品列表{}", itemPageQuery);
        return itemService.listItem(itemPageQuery);
    }

    @GetMapping("/{id}")
    @ApiOperation("查询商品详情")
    @Cacheable(cacheNames = "item", key = "'id:' + #id")
    public AjaxResult<ItemDetailVO> getItemById(@PathVariable Long id) {
        log.info("开始执行查询商品详情{}", id);
        return AjaxResult.success(itemService.getItemById(id));
    }

    @GetMapping("/batch")
    @ApiOperation("批量查询商品详情")
    @Cacheable(cacheNames = "item", key = "'list:' + #ids")
    public AjaxResult<List<ItemDetailVO>> getItemByIds(@RequestParam("ids") List<Long> ids) {
        log.info("开始执行批量查询商品详情{}", ids);
        return AjaxResult.success(itemService.getItemByIds(ids));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除商品")
    @CacheEvict(cacheNames = "item", allEntries = true)
    public AjaxResult<Void> deleteById(@PathVariable Long id) {
        log.info("开始执行删除商品{}", id);
        return AjaxResult.toAjax(itemService.removeById(id));
    }

    @DeleteMapping
    @ApiOperation("批量删除商品")
    @CacheEvict(cacheNames = "item", allEntries = true)
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        log.info("开始执行批量删除商品{}", ids);

        return AjaxResult.toAjax(itemService.removeByIds(ids));
    }

    @PostMapping
    @ApiOperation("添加商品")
    @CacheEvict(cacheNames = "item", allEntries = true)
    public AjaxResult<Void> add(@RequestBody ItemDTO itemDTO) {
        log.info("开始执行添加商品");
        itemService.add(itemDTO);
        return AjaxResult.success();
    }

    @PutMapping
    @ApiOperation("修改商品")
    public AjaxResult<Void> update( @RequestBody ItemDTO itemDTO) {
        log.info("开始执行修改商品,参数为:{}", itemDTO);
        itemService.updateItemById(itemDTO);
        return AjaxResult.success();
    }

    @GetMapping("/dashboard")
    @ApiOperation("获取商品管理看板数据")
    public AjaxResult<ItemDashboardVO> getDashboardData() {
        log.info("开始查询商品管理看板数据");
        return AjaxResult.success(itemService.getItemDashboardData());
    }

    @ApiOperation("导出Excel表")
    @PostMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        log.info("开始进行订单导出");
        List<Item> list1 = itemService.list();
        ExcelUtils.exportEasyExcel(response, list1, Item.class, "商品列表");
    }


}
