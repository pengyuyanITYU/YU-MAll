package com.yu.item.controller;

import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.UnauthorizedException;
import com.yu.common.utils.AdministratorContext;
import com.yu.item.domain.dto.ShopDTO;
import com.yu.item.domain.query.ShopPageQuery;
import com.yu.item.domain.vo.ShopVO;
import com.yu.item.service.IShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/shops")
@RequiredArgsConstructor
@Api(tags = "店铺管理")
public class AdminShopController {

    private final IShopService shopService;

    @ModelAttribute
    public void checkAdminLogin() {
        if (AdministratorContext.getUser() == null) {
            throw new UnauthorizedException("unauthorized");
        }
    }

    @GetMapping("/list")
    @ApiOperation("分页查询店铺")
    public TableDataInfo list(ShopPageQuery query) {
        return shopService.listShops(query);
    }

    @GetMapping("/simple")
    @ApiOperation("查询店铺简要列表")
    public AjaxResult<List<ShopVO>> simpleList() {
        return AjaxResult.success(shopService.listSimple());
    }

    @GetMapping("/{id}")
    @ApiOperation("查询店铺详情")
    public AjaxResult<ShopVO> getById(@PathVariable Long id) {
        return AjaxResult.success(shopService.getShopById(id));
    }

    @PostMapping
    @ApiOperation("新增店铺")
    public AjaxResult<Void> add(@Validated @RequestBody ShopDTO shopDTO) {
        return AjaxResult.toAjax(shopService.addShop(shopDTO));
    }

    @PutMapping
    @ApiOperation("修改店铺")
    public AjaxResult<Void> update(@Validated @RequestBody ShopDTO shopDTO) {
        return AjaxResult.toAjax(shopService.updateShop(shopDTO));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除店铺")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return AjaxResult.toAjax(shopService.deleteShop(id));
    }
}
