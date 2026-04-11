package com.yu.item.controller;

import com.yu.api.dto.admin.AdminItemDTO;
import com.yu.api.dto.admin.AdminItemSkuDTO;
import com.yu.api.dto.admin.AdminSpecTemplateDTO;
import com.yu.api.query.admin.AdminItemPageQuery;
import com.yu.api.vo.admin.AdminItemExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.ItemDTO;
import com.yu.item.domain.dto.ItemSkuDTO;
import com.yu.item.domain.po.Item;
import com.yu.item.domain.po.SpecTemplate;
import com.yu.item.domain.query.ItemPageQuery;
import com.yu.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/items")
public class InternalAdminItemController {

    private final IItemService itemService;

    @GetMapping("/list")
    public TableDataInfo list(AdminItemPageQuery query) {
        return itemService.listAdminItems(toItemPageQuery(query));
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return AjaxResult.success(itemService.getItemById(id));
    }

    @GetMapping("/batch")
    public AjaxResult<Object> getByIds(@RequestParam("ids") List<Long> ids) {
        return AjaxResult.success(itemService.getItemByIds(ids));
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return AjaxResult.toAjax(itemService.removeById(id));
    }

    @DeleteMapping
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        return AjaxResult.toAjax(itemService.removeByIds(ids));
    }

    @PostMapping
    public AjaxResult<Void> add(@Validated @RequestBody AdminItemDTO itemDTO) {
        itemService.add(toItemDTO(itemDTO));
        return AjaxResult.success();
    }

    @PutMapping
    public AjaxResult<Void> update(@Validated @RequestBody AdminItemDTO itemDTO) {
        itemService.updateItemById(toItemDTO(itemDTO));
        return AjaxResult.success();
    }

    @GetMapping("/dashboard")
    public AjaxResult<Object> dashboard() {
        return AjaxResult.success(itemService.getItemDashboardData());
    }

    @GetMapping("/export-data")
    public AjaxResult<List<AdminItemExportVO>> exportData() {
        return AjaxResult.success(itemService.list().stream().map(this::toExportVO).collect(Collectors.toList()));
    }

    private ItemPageQuery toItemPageQuery(AdminItemPageQuery query) {
        ItemPageQuery target = new ItemPageQuery();
        if (query != null) {
            BeanUtils.copyProperties(query, target);
        }
        return target;
    }

    private ItemDTO toItemDTO(AdminItemDTO source) {
        ItemDTO target = new ItemDTO();
        BeanUtils.copyProperties(source, target);
        target.setSpecTemplate(toSpecTemplates(source.getSpecTemplate()));
        target.setSkus(toSkuList(source.getSkus()));
        return target;
    }

    private List<SpecTemplate> toSpecTemplates(List<AdminSpecTemplateDTO> source) {
        if (source == null) {
            return Collections.emptyList();
        }
        return source.stream().map(item -> {
            SpecTemplate template = new SpecTemplate();
            BeanUtils.copyProperties(item, template);
            return template;
        }).collect(Collectors.toList());
    }

    private List<ItemSkuDTO> toSkuList(List<AdminItemSkuDTO> source) {
        if (source == null) {
            return Collections.emptyList();
        }
        return source.stream().map(item -> {
            ItemSkuDTO skuDTO = new ItemSkuDTO();
            BeanUtils.copyProperties(item, skuDTO);
            return skuDTO;
        }).collect(Collectors.toList());
    }

    private AdminItemExportVO toExportVO(Item item) {
        AdminItemExportVO exportVO = new AdminItemExportVO();
        BeanUtils.copyProperties(item, exportVO);
        return exportVO;
    }
}
