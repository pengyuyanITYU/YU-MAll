package com.yu.item.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.comment.mapper.CommentMapper;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.po.Item;
import com.yu.item.domain.po.Shop;
import com.yu.item.domain.query.ItemPageQuery;
import com.yu.item.domain.vo.ItemCommentStatsVO;
import com.yu.item.domain.vo.ItemDetailVO;
import com.yu.item.domain.vo.ItemListVO;
import com.yu.item.mapper.ItemDetailMapper;
import com.yu.item.mapper.ItemMapper;
import com.yu.item.mapper.ItemSkuMapper;
import com.yu.item.service.IItemDetailService;
import com.yu.item.service.IItemSkuService;
import com.yu.item.service.IShopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ItemDetailMapper itemDetailMapper;

    @Mock
    private ItemSkuMapper itemSkuMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private IItemDetailService itemDetailService;

    @Mock
    private IItemSkuService itemSkuService;

    @Mock
    private IShopService shopService;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void listItemShouldReturnShopTrustFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("测试商品");
        item.setShopId(2L);
        item.setStatus(1);

        Shop shop = new Shop();
        shop.setId(2L);
        shop.setName("官方旗舰店");
        shop.setIsSelf(1);
        shop.setShippingType("FIXED");
        shop.setShippingFee(800);

        ItemCommentStatsVO stats = new ItemCommentStatsVO();
        stats.setItemId(1L);
        stats.setApprovedCount(10);
        stats.setPositiveCount(8);

        when(itemMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            IPage<Item> page = invocation.getArgument(0, IPage.class);
            page.setRecords(List.of(item));
            page.setTotal(1L);
            return page;
        });
        when(shopService.listByIds(anyList())).thenReturn(List.of(shop));
        when(commentMapper.selectItemStatsByItemIds(anyList())).thenReturn(List.of(stats));

        TableDataInfo result = itemService.listItem(new ItemPageQuery());
        ItemListVO row = (ItemListVO) result.getRows().get(0);

        assertEquals("官方旗舰店", row.getShopName());
        assertEquals("运费8.00元", row.getShippingDesc());
        assertEquals(80, row.getPositiveRate());
        assertEquals(10, row.getCommentCount());
    }

    @Test
    void getItemByIdShouldReturnShopTrustAndPositiveRate() {
        Item item = new Item();
        item.setId(1L);
        item.setName("测试商品");
        item.setShopId(2L);
        item.setStatus(1);

        Shop shop = new Shop();
        shop.setId(2L);
        shop.setName("官方旗舰店");
        shop.setIsSelf(1);
        shop.setShippingType("THRESHOLD_FREE");
        shop.setShippingFee(1000);
        shop.setFreeShippingThreshold(9900);

        ItemCommentStatsVO stats = new ItemCommentStatsVO();
        stats.setItemId(1L);
        stats.setApprovedCount(5);
        stats.setPositiveCount(4);

        when(itemMapper.selectById(eq(1L))).thenReturn(item);
        when(itemDetailMapper.selectOne(any())).thenReturn(null);
        when(itemSkuMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(shopService.getById(eq(2L))).thenReturn(shop);
        when(commentMapper.selectItemStatsByItemIds(anyList())).thenReturn(List.of(stats));

        ItemDetailVO detail = itemService.getItemById(1L);

        assertNotNull(detail);
        assertEquals("官方旗舰店", detail.getShopName());
        assertEquals("满99.00元包邮", detail.getShippingDesc());
        assertEquals(80, detail.getPositiveRate());
        assertEquals(5, detail.getCommentCount());
    }

    @Test
    void listItemShouldNotFallbackToLegacyCommentCountWhenApprovedCommentsMissing() {
        Item item = new Item();
        item.setId(1L);
        item.setName("测试商品");
        item.setShopId(2L);
        item.setStatus(1);
        item.setCommentCount(90);

        Shop shop = new Shop();
        shop.setId(2L);
        shop.setName("官方旗舰店");
        shop.setIsSelf(1);
        shop.setShippingType("FREE");

        when(itemMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            IPage<Item> page = invocation.getArgument(0, IPage.class);
            page.setRecords(List.of(item));
            page.setTotal(1L);
            return page;
        });
        when(shopService.listByIds(anyList())).thenReturn(List.of(shop));
        when(commentMapper.selectItemStatsByItemIds(anyList())).thenReturn(Collections.emptyList());

        TableDataInfo result = itemService.listItem(new ItemPageQuery());
        ItemListVO row = (ItemListVO) result.getRows().get(0);

        assertEquals(0, row.getCommentCount());
        assertNull(row.getPositiveRate());
    }
}
