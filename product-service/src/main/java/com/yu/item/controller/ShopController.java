package com.yu.item.controller;

import com.yu.common.domain.AjaxResult;
import com.yu.item.domain.vo.ShopVO;
import com.yu.item.service.IShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shops")
public class ShopController {

    private final IShopService shopService;

    @GetMapping("/{id}")
    public AjaxResult<ShopVO> getById(@PathVariable Long id) {
        return AjaxResult.success(shopService.getEnabledShopById(id));
    }
}
