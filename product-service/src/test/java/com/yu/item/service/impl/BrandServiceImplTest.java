package com.yu.item.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.yu.item.domain.dto.BrandDTO;
import com.yu.item.domain.po.Brand;
import com.yu.item.domain.po.Item;
import com.yu.item.service.IItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

    @Mock
    private IItemService itemService;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<Brand> brandQueryWrapper;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<Item> itemQueryWrapper;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaUpdateChainWrapper<Item> itemUpdateWrapper;

    private BrandServiceImpl brandService;

    @BeforeEach
    void setUp() {
        brandService = spy(new BrandServiceImpl(itemService));
    }

    @Test
    void updateBrandShouldSyncBoundItemBrandWhenNameChanges() {
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("old-brand");

        doReturn(brandQueryWrapper).when(brandService).lambdaQuery();
        doReturn(brand).when(brandService).getById(1L);
        doReturn(true).when(brandService).updateById(any(Brand.class));
        when(brandQueryWrapper.eq(any(), eq("new-brand"))).thenReturn(brandQueryWrapper);
        when(brandQueryWrapper.ne(any(), eq(1L))).thenReturn(brandQueryWrapper);
        when(brandQueryWrapper.count()).thenReturn(0L);

        when(itemService.lambdaQuery()).thenReturn(itemQueryWrapper);
        when(itemQueryWrapper.eq(any(), eq("old-brand"))).thenReturn(itemQueryWrapper);
        when(itemQueryWrapper.count()).thenReturn(2L);

        when(itemService.lambdaUpdate()).thenReturn(itemUpdateWrapper);
        when(itemUpdateWrapper.eq(any(), eq("old-brand"))).thenReturn(itemUpdateWrapper);
        when(itemUpdateWrapper.set(any(), eq("new-brand"))).thenReturn(itemUpdateWrapper);
        when(itemUpdateWrapper.update()).thenReturn(true);

        BrandDTO brandDTO = new BrandDTO();
        brandDTO.setId(1L);
        brandDTO.setName("new-brand");

        boolean updated = brandService.updateBrand(brandDTO);

        assertTrue(updated);
        ArgumentCaptor<Brand> brandCaptor = ArgumentCaptor.forClass(Brand.class);
        verify(brandService).updateById(brandCaptor.capture());
        assertEquals("new-brand", brandCaptor.getValue().getName());
        verify(itemUpdateWrapper).eq(any(), eq("old-brand"));
        verify(itemUpdateWrapper).set(any(), eq("new-brand"));
        verify(itemUpdateWrapper).update();
    }
}
