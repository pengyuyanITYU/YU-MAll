package com.yu.cart.controller;

import com.yu.cart.domain.dto.CartFormDTO;
import com.yu.cart.domain.po.Cart;
import com.yu.cart.service.ICartService;
import com.yu.common.domain.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "购物车接口")
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final ICartService cartService;

    @ApiOperation("新增购物车商品")
    @PostMapping
    public AjaxResult addItemCart(@Valid @RequestBody CartFormDTO cartFormDTO) {
        log.info("新增购物车商品: {}", cartFormDTO);
        cartService.addItemCart(cartFormDTO);
        return AjaxResult.success();
    }

    @ApiOperation("更新购物车商品")
    @PutMapping
    public AjaxResult updateCart(@RequestBody Cart cart) {
        cartService.updateById(cart);
        return AjaxResult.success();
    }

    @ApiOperation("删除购物车商品")
    @DeleteMapping("{id}")
    public AjaxResult deleteCartItem(@PathVariable("id") Long id) {
        return AjaxResult.toAjax(cartService.removeById(id));
    }

    @ApiOperation("查询我的购物车")
    @GetMapping
    public AjaxResult queryMyCarts(@RequestHeader(name = "user-info", required = false) String userInfo) {
        log.info("查询我的购物车, user-info={}", userInfo);
        return AjaxResult.success(cartService.queryMyCarts());
    }

    @ApiOperation("根据ids批量删除购物车商品")
    @ApiImplicitParam(name = "ids", value = "购物车商品id集合")
    @DeleteMapping
    public AjaxResult deleteCartItemByIds(@RequestParam("ids") List<Long> ids) {
        return AjaxResult.toAjax(cartService.removeByIds(ids));
    }
}