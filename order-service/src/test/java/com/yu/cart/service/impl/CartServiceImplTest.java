package com.yu.cart.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.yu.api.client.ItemClient;
import com.yu.api.vo.ItemDetailVO;
import com.yu.cart.domain.dto.CartFormDTO;
import com.yu.cart.domain.po.Cart;
import com.yu.cart.domain.vo.CartVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private ItemClient itemClient;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<Cart> lambdaQueryWrapper;

    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        cartService = spy(new CartServiceImpl(itemClient));
        UserContext.setUser(88L);
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
    }

    @Test
    void cartDomain_shouldExposeSkuIdAcrossRequestStorageAndResponse() {
        assertDoesNotThrow(() -> CartFormDTO.class.getDeclaredField("skuId"));
        assertDoesNotThrow(() -> Cart.class.getDeclaredField("skuId"));
        assertDoesNotThrow(() -> CartVO.class.getDeclaredField("skuId"));
    }

    @Test
    void addItemCart_shouldUseSpecAsDeduplicationFallback_whenSkuIdMissing() {
        doReturn(lambdaQueryWrapper).when(cartService).lambdaQuery();
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.one()).thenReturn(null);
        doReturn(true).when(cartService).save(any(Cart.class));

        ItemDetailVO itemDetailVO = new ItemDetailVO();
        itemDetailVO.setId(1001L);
        itemDetailVO.setName("测试商品");
        when(itemClient.getItemById(1001L)).thenReturn(AjaxResult.success(itemDetailVO));

        CartFormDTO formDTO = new CartFormDTO();
        formDTO.setItemId(1001L);
        formDTO.setNum(1);
        formDTO.setName("测试商品");
        formDTO.setImage("test.png");
        formDTO.setPrice(199900L);
        formDTO.setSpec("{\"颜色\":\"黑色\"}");

        cartService.addItemCart(formDTO);

        verify(lambdaQueryWrapper, atLeastOnce()).eq(any(), eq("{\"颜色\":\"黑色\"}"));
    }

    @Test
    void addItemCart_shouldDeduplicateBySkuOnly_whenSkuIdPresent() {
        doReturn(lambdaQueryWrapper).when(cartService).lambdaQuery();
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);

        Cart existing = new Cart();
        existing.setId(1L);
        existing.setNum(1);
        existing.setSkuId(136L);
        existing.setSpec("{\"颜色\":\"黑色\"}");
        when(lambdaQueryWrapper.one()).thenReturn(existing);
        doReturn(true).when(cartService).updateById(any(Cart.class));

        ItemDetailVO itemDetailVO = new ItemDetailVO();
        itemDetailVO.setId(1001L);
        itemDetailVO.setName("测试商品");
        when(itemClient.getItemById(1001L)).thenReturn(AjaxResult.success(itemDetailVO));

        CartFormDTO formDTO = new CartFormDTO();
        formDTO.setItemId(1001L);
        formDTO.setSkuId(136L);
        formDTO.setNum(1);
        formDTO.setName("测试商品");
        formDTO.setImage("test.png");
        formDTO.setPrice(199900L);
        formDTO.setSpec("{\"颜色\":\"黑色\"}");

        cartService.addItemCart(formDTO);

        verify(lambdaQueryWrapper, times(1)).eq(any(), eq(136L));
        verify(lambdaQueryWrapper, never()).eq(any(), eq("{\"颜色\":\"黑色\"}"));
        verify(cartService, times(1)).updateById(any(Cart.class));
        verify(cartService, never()).save(any(Cart.class));
    }
}
