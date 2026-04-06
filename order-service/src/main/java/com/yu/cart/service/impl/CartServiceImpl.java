package com.yu.cart.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.client.ItemClient;
import com.yu.api.vo.ItemDetailVO;
import com.yu.cart.domain.dto.CartFormDTO;
import com.yu.cart.domain.po.Cart;
import com.yu.cart.domain.vo.CartVO;
import com.yu.cart.mapper.CartMapper;
import com.yu.cart.service.ICartService;
import com.yu.common.domain.AjaxResult;
import com.yu.common.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Api(tags = "购物车相关接口")
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final ItemClient itemClient;

    @Override
    @ApiOperation("添加商品到购物车")
    public void addItemCart(CartFormDTO cartFormDTO) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new RuntimeException("未获取到用户信息，请检查登录状态");
        }

        AjaxResult<ItemDetailVO> itemResult = itemClient.getItemById(cartFormDTO.getItemId());
        if (itemResult == null || !itemResult.isSuccess() || itemResult.getData() == null) {
            throw new RuntimeException("商品不存在");
        }

        ItemDetailVO item = itemResult.getData();
        if (cartFormDTO.getName() == null) {
            cartFormDTO.setName(item.getName());
        }

        String specJson = normalizeSpecToJson(cartFormDTO.getSpec());
        Long skuId = normalizeSkuId(cartFormDTO.getSkuId());

        var query = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getItemId, cartFormDTO.getItemId());
        if (skuId != null) {
            query.eq(Cart::getSkuId, skuId);
        } else if (specJson != null && !specJson.isBlank()) {
            query.eq(Cart::getSpec, specJson);
        }

        Cart existing = query.one();
        if (existing != null) {
            existing.setNum(existing.getNum() + cartFormDTO.getNum());
            existing.setSpec(specJson);
            existing.setSkuId(skuId);
            updateById(existing);
            return;
        }

        Cart cart = new Cart()
                .setItemId(cartFormDTO.getItemId())
                .setNum(cartFormDTO.getNum())
                .setImage(cartFormDTO.getImage())
                .setSpec(specJson)
                .setSkuId(skuId)
                .setPrice(cartFormDTO.getPrice())
                .setName(cartFormDTO.getName())
                .setUserId(userId);
        save(cart);
    }

    @Override
    public List<CartVO> queryMyCarts() {
        Long userId = UserContext.getUser();
        List<Cart> list = lambdaQuery().eq(Cart::getUserId, userId).list();
        return list.stream().map(cart -> {
            CartVO cartVO = new CartVO();
            cartVO.setId(cart.getId());
            cartVO.setItemId(cart.getItemId());
            cartVO.setNum(cart.getNum());
            cartVO.setName(cart.getName());
            cartVO.setSpec(cart.getSpec());
            cartVO.setSkuId(cart.getSkuId());
            cartVO.setPrice(cart.getPrice());
            cartVO.setImage(cart.getImage());
            return cartVO;
        }).collect(Collectors.toList());
    }

    private String normalizeSpecToJson(String spec) {
        if (spec == null || spec.isBlank()) {
            return null;
        }
        String trimmed = spec.trim();
        if (JSONUtil.isTypeJSON(trimmed) && trimmed.startsWith("{")) {
            var source = JSONUtil.parseObj(trimmed);
            var normalized = JSONUtil.createObj();
            source.keySet().stream()
                    .sorted()
                    .forEach(key -> normalized.set(key, source.get(key)));
            return normalized.toString();
        }
        return JSONUtil.createObj().set("规格", trimmed).toString();
    }

    private Long normalizeSkuId(Long skuId) {
        if (skuId == null || skuId <= 0) {
            return null;
        }
        return skuId;
    }
}
