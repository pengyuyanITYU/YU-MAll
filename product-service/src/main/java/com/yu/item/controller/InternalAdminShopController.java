package com.yu.item.controller;

import com.yu.api.dto.admin.AdminShopDTO;
import com.yu.api.query.admin.AdminShopPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.ShopDTO;
import com.yu.item.domain.query.ShopPageQuery;
import com.yu.item.service.IShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/shops")
public class InternalAdminShopController {

    private final IShopService shopService;

    @GetMapping("/list")
    public TableDataInfo list(AdminShopPageQuery query) {
        ShopPageQuery target = new ShopPageQuery();
        if (query != null) {
            BeanUtils.copyProperties(query, target);
        }
        return shopService.listShops(target);
    }

    @GetMapping("/simple")
    public AjaxResult<Object> simpleList() {
        return AjaxResult.success(shopService.listSimple());
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return AjaxResult.success(shopService.getShopById(id));
    }

    @PostMapping
    public AjaxResult<Void> add(@Validated @RequestBody AdminShopDTO shopDTO) {
        return AjaxResult.toAjax(shopService.addShop(toShopDTO(shopDTO)));
    }

    @PutMapping
    public AjaxResult<Void> update(@Validated @RequestBody AdminShopDTO shopDTO) {
        return AjaxResult.toAjax(shopService.updateShop(toShopDTO(shopDTO)));
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return AjaxResult.toAjax(shopService.deleteShop(id));
    }

    private ShopDTO toShopDTO(AdminShopDTO source) {
        ShopDTO target = new ShopDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
