package com.yu.item.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
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
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Item.class);
        TableInfoHelper.initTableInfo(assistant, Shop.class);
    }

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
    @Test
    void listItemShouldOnlyReturnOnShelfItemsForPublicQuery() {
        Item onShelf = new Item();
        onShelf.setId(1L);
        onShelf.setName("on shelf");
        onShelf.setStatus(1);
        onShelf.setShopId(9L);

        Item offShelf = new Item();
        offShelf.setId(2L);
        offShelf.setName("off shelf");
        offShelf.setStatus(2);
        offShelf.setShopId(9L);

        when(itemMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            IPage<Item> page = invocation.getArgument(0, IPage.class);
            @SuppressWarnings("unchecked")
            com.baomidou.mybatisplus.core.conditions.Wrapper<Item> wrapper = invocation.getArgument(1, com.baomidou.mybatisplus.core.conditions.Wrapper.class);
            String sqlSegment = wrapper == null ? "" : String.valueOf(wrapper.getSqlSegment());
            boolean onlyOnShelf = sqlSegment.contains("status");
            page.setRecords(onlyOnShelf ? List.of(onShelf) : List.of(onShelf, offShelf));
            page.setTotal(onlyOnShelf ? 1L : 2L);
            return page;
        });
        when(shopService.listByIds(anyList())).thenReturn(Collections.emptyList());
        when(commentMapper.selectItemStatsByItemIds(anyList())).thenReturn(Collections.emptyList());

        TableDataInfo result = itemService.listItem(new ItemPageQuery());

        assertEquals(1, result.getRows().size());
        ItemListVO row = (ItemListVO) result.getRows().get(0);
        assertEquals("on shelf", row.getName());
    }

    @Test
    void listAdminItemsShouldNotApplyOnShelfFilter() throws Exception {
        Item onShelf = new Item();
        onShelf.setId(1L);
        onShelf.setName("on shelf");
        onShelf.setStatus(1);
        onShelf.setStock(12);
        onShelf.setUpdateTime(LocalDateTime.of(2026, 4, 7, 19, 0));
        onShelf.setShopId(9L);

        Item offShelf = new Item();
        offShelf.setId(2L);
        offShelf.setName("off shelf");
        offShelf.setStatus(2);
        offShelf.setStock(3);
        offShelf.setUpdateTime(LocalDateTime.of(2026, 4, 7, 19, 1));
        offShelf.setShopId(9L);

        when(itemMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            IPage<Item> page = invocation.getArgument(0, IPage.class);
            @SuppressWarnings("unchecked")
            com.baomidou.mybatisplus.core.conditions.Wrapper<Item> wrapper = invocation.getArgument(1, com.baomidou.mybatisplus.core.conditions.Wrapper.class);
            String sqlSegment = wrapper == null ? "" : String.valueOf(wrapper.getSqlSegment());
            boolean onlyOnShelf = sqlSegment.contains("status");
            page.setRecords(onlyOnShelf ? List.of(onShelf) : List.of(onShelf, offShelf));
            page.setTotal(onlyOnShelf ? 1L : 2L);
            return page;
        });
        when(shopService.listByIds(anyList())).thenReturn(Collections.emptyList());
        when(commentMapper.selectItemStatsByItemIds(anyList())).thenReturn(Collections.emptyList());

        Method method = ItemServiceImpl.class.getMethod("listAdminItems", ItemPageQuery.class);
        TableDataInfo result = (TableDataInfo) method.invoke(itemService, new ItemPageQuery());

        assertEquals(2, result.getRows().size());
        ItemListVO first = (ItemListVO) result.getRows().get(0);
        ItemListVO second = (ItemListVO) result.getRows().get(1);
        assertEquals(1, first.getStatus());
        assertEquals(12, first.getStock());
        assertNotNull(first.getUpdateTime());
        assertEquals(2, second.getStatus());
        assertEquals(3, second.getStock());
        assertNotNull(second.getUpdateTime());
    }

    @Test
    void listItemShouldApplyShopIdFilterWhenPresent() {
        Item matchedItem = new Item();
        matchedItem.setId(1L);
        matchedItem.setName("shop item");
        matchedItem.setStatus(1);
        matchedItem.setShopId(7L);

        Item otherShopItem = new Item();
        otherShopItem.setId(2L);
        otherShopItem.setName("other shop item");
        otherShopItem.setStatus(1);
        otherShopItem.setShopId(8L);

        ItemPageQuery query = new ItemPageQuery();
        BeanWrapper beanWrapper = new BeanWrapperImpl(query);
        assertTrue(beanWrapper.isWritableProperty("shopId"), "ItemPageQuery 缺少 shopId 查询参数");
        beanWrapper.setPropertyValue("shopId", 7L);

        when(itemMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            IPage<Item> page = invocation.getArgument(0, IPage.class);
            @SuppressWarnings("unchecked")
            com.baomidou.mybatisplus.core.conditions.Wrapper<Item> wrapper = invocation.getArgument(1, com.baomidou.mybatisplus.core.conditions.Wrapper.class);
            String sqlSegment = wrapper == null ? "" : String.valueOf(wrapper.getSqlSegment());
            boolean hasShopFilter = sqlSegment.contains("shop_id");
            page.setRecords(hasShopFilter ? List.of(matchedItem) : List.of(matchedItem, otherShopItem));
            page.setTotal(hasShopFilter ? 1L : 2L);
            return page;
        });
        when(shopService.listByIds(anyList())).thenReturn(Collections.emptyList());
        when(commentMapper.selectItemStatsByItemIds(anyList())).thenReturn(Collections.emptyList());

        TableDataInfo result = itemService.listItem(query);

        assertEquals(1, result.getRows().size());
        ItemListVO row = (ItemListVO) result.getRows().get(0);
        assertEquals(7L, row.getShopId());
        assertEquals("shop item", row.getName());
    }
}
