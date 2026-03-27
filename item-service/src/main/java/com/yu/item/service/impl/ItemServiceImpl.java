package com.yu.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.common.constant.HttpStatus;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.BusinessException;
import com.yu.common.utils.BeanUtils;
import com.yu.common.utils.CollUtils;
import com.yu.item.domain.dto.ItemDTO;
import com.yu.item.domain.dto.ItemSkuDTO;
import com.yu.item.domain.dto.OrderDetailDTO;
import com.yu.item.domain.po.Item;
import com.yu.item.domain.po.ItemDetail;
import com.yu.item.domain.po.ItemSku;
import com.yu.item.domain.query.ItemPageQuery;
import com.yu.item.domain.vo.ItemDashboardVO;
import com.yu.item.domain.vo.ItemDetailVO;
import com.yu.item.mapper.ItemDetailMapper;
import com.yu.item.mapper.ItemMapper;
import com.yu.item.mapper.ItemSkuMapper;
import com.yu.item.service.ICategoryService;
import com.yu.item.service.IItemDetailService;
import com.yu.item.service.IItemService;
import com.yu.item.service.IItemSkuService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    private final ItemMapper itemMapper;
    private final ItemDetailMapper itemDetailMapper;
    private final ItemSkuMapper itemSkuMapper;
    private final IItemDetailService itemDetailService;
    private final IItemSkuService itemSkuService;

    private ICategoryService categoryService;

    @Autowired
    @Lazy
    public void setCategoryService(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void updateStockAndSold(List<OrderDetailDTO> orderDetailDTOList) {
        if (CollUtils.isEmpty(orderDetailDTOList)) {
            return;
        }
        List<Item> items = new ArrayList<>();
        for (OrderDetailDTO orderDetailDTO : orderDetailDTOList) {
            Item item = lambdaQuery()
                    .eq(Item::getId, orderDetailDTO.getItemId())
                    .eq(Item::getStatus, 1)
                    .one();
            if (item == null) {
                continue;
            }
            int sold = item.getSold() == null ? 0 : item.getSold();
            int stock = item.getStock() == null ? 0 : item.getStock();
            item.setSold(sold + orderDetailDTO.getNum());
            item.setStock(stock - orderDetailDTO.getNum());
            items.add(item);
        }
        if (CollUtils.isEmpty(items)) {
            return;
        }
        boolean ok = updateBatchById(items);
        if (!ok) {
            throw new BusinessException("更新库存失败");
        }
    }

    @Override
    public TableDataInfo listItem(ItemPageQuery itemPageQuery) {
        ItemPageQuery query = itemPageQuery == null ? new ItemPageQuery() : itemPageQuery;
        Page<Item> page = new Page<>(query.getPageNo(), query.getPageSize());
        if (StrUtil.isNotBlank(query.getSold())) {
            page.addOrder(OrderItem.desc("sold"));
        }
        if (StrUtil.isNotBlank(query.getSortBy())) {
            page.addOrder(Boolean.TRUE.equals(query.getIsAsc()) ? OrderItem.asc(query.getSortBy()) : OrderItem.desc(query.getSortBy()));
        } else {
            page.addOrder(OrderItem.desc("update_time"));
        }
        Page<Item> result = lambdaQuery()
                .eq(Item::getStatus, 1)
                .like(StrUtil.isNotBlank(query.getName()), Item::getName, query.getName())
                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
                .ge(query.getMinPrice() != null, Item::getPrice, query.getMinPrice())
                .le(query.getMaxPrice() != null, Item::getPrice, query.getMaxPrice())
                .page(page);
        TableDataInfo tableDataInfo = new TableDataInfo();
        tableDataInfo.setCode(HttpStatus.SUCCESS);
        tableDataInfo.setMsg("查询成功");
        tableDataInfo.setRows(result.getRecords());
        tableDataInfo.setTotal(result.getTotal());
        return tableDataInfo;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void add(ItemDTO itemDTO) {
        checkItemDTO(itemDTO, false);
        Item item = new Item();
        item.setName(itemDTO.getName());
        item.setSubTitle(itemDTO.getSubTitle());
        item.setImage(itemDTO.getImage());
        item.setPrice(itemDTO.getPrice());
        item.setOriginalPrice(itemDTO.getOriginalPrice());
        item.setStock(itemDTO.getStock() != null ? itemDTO.getStock() : calcTotalSkuStock(itemDTO.getSkus()));
        item.setTags(itemDTO.getTags());
        item.setStatus(itemDTO.getStatus());
        item.setCategory(itemDTO.getCategory());
        item.setBrand(itemDTO.getBrand());
        item.setCategoryId(itemDTO.getCategoryId());
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        if (!save(item)) {
            throw new BusinessException("商品新增失败");
        }

        ItemDetail detail = new ItemDetail();
        detail.setItemId(item.getId());
        detail.setBannerImages(JSON.toJSONString(itemDTO.getBannerImages()));
        detail.setDetailHtml(itemDTO.getDetailHtml());
        detail.setSpecTemplate(JSON.toJSONString(itemDTO.getSpecTemplate()));
        detail.setVideoUrl(itemDTO.getVideoUrl());
        if (!itemDetailService.save(detail)) {
            throw new BusinessException("商品详情新增失败");
        }

        List<ItemSku> skuList = buildSkuList(item.getId(), itemDTO.getSkus());
        if (CollUtils.isNotEmpty(skuList) && !itemSkuService.saveBatch(skuList)) {
            throw new BusinessException("商品SKU新增失败");
        }
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void updateItemById(ItemDTO itemDTO) {
        checkItemDTO(itemDTO, true);
        Item item = getById(itemDTO.getId());
        if (item == null) {
            throw new BusinessException("商品不存在");
        }
        BeanUtils.copyProperties(itemDTO, item);
        if (itemDTO.getStock() == null && CollUtils.isNotEmpty(itemDTO.getSkus())) {
            item.setStock(calcTotalSkuStock(itemDTO.getSkus()));
        }
        if (!updateById(item)) {
            throw new BusinessException("商品更新失败");
        }

        ItemDetail detail = itemDetailService.lambdaQuery().eq(ItemDetail::getItemId, item.getId()).one();
        if (detail == null) {
            detail = new ItemDetail();
            detail.setItemId(item.getId());
        }
        detail.setBannerImages(JSON.toJSONString(itemDTO.getBannerImages()));
        detail.setDetailHtml(itemDTO.getDetailHtml());
        detail.setSpecTemplate(JSON.toJSONString(itemDTO.getSpecTemplate()));
        detail.setVideoUrl(itemDTO.getVideoUrl());
        boolean detailOk = detail.getId() == null ? itemDetailService.save(detail) : itemDetailService.updateById(detail);
        if (!detailOk) {
            throw new BusinessException("商品详情更新失败");
        }

        itemSkuService.lambdaUpdate().eq(ItemSku::getItemId, item.getId()).remove();
        List<ItemSku> skuList = buildSkuList(item.getId(), itemDTO.getSkus());
        if (CollUtils.isNotEmpty(skuList) && !itemSkuService.saveBatch(skuList)) {
            throw new BusinessException("商品SKU更新失败");
        }
    }

    @Override
    public ItemDetailVO getItemById(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            return null;
        }
        ItemDetail itemDetail = itemDetailMapper.selectOne(new LambdaQueryWrapper<ItemDetail>().eq(ItemDetail::getItemId, id));
        List<ItemSku> skuList = itemSkuMapper.selectList(new LambdaQueryWrapper<ItemSku>().eq(ItemSku::getItemId, id));
        return convertDetail(item, itemDetail, skuList);
    }

    @Override
    public List<ItemDetailVO> getItemByIds(List<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<Item> items = listByIds(ids);
        if (CollUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<Long> validIds = items.stream().map(Item::getId).collect(Collectors.toList());
        List<ItemDetail> detailList = itemDetailMapper.selectList(new LambdaQueryWrapper<ItemDetail>().in(ItemDetail::getItemId, validIds));
        Map<Long, ItemDetail> detailMap = detailList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ItemDetail::getItemId, Function.identity(), (a, b) -> a));
        List<ItemSku> allSkuList = itemSkuMapper.selectList(new LambdaQueryWrapper<ItemSku>().in(ItemSku::getItemId, validIds));
        Map<Long, List<ItemSku>> skuMap = allSkuList.stream().collect(Collectors.groupingBy(ItemSku::getItemId));
        return items.stream()
                .map(item -> convertDetail(item, detailMap.get(item.getId()), skuMap.get(item.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDashboardVO getItemDashboardData() {
        Long totalItems = count();
        Long onShelfItems = count(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 1));
        Long offShelfItems = count(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 2));
        Long lowStockItems = itemMapper.countLowStock();
        Long totalCategories = categoryService == null ? 0L : categoryService.count();
        List<ItemDashboardVO.ChartDataVO> categoryDist = itemMapper.selectCategoryDistribution().stream()
                .map(m -> ItemDashboardVO.ChartDataVO.builder()
                        .name((String) m.get("name"))
                        .value(m.get("value"))
                        .build())
                .collect(Collectors.toList());
        List<ItemDashboardVO.ChartDataVO> topSelling = itemMapper.selectTopSelling().stream()
                .map(m -> ItemDashboardVO.ChartDataVO.builder()
                        .name((String) m.get("name"))
                        .value(m.get("value"))
                        .build())
                .collect(Collectors.toList());
        return ItemDashboardVO.builder()
                .totalItems(totalItems)
                .onShelfItems(onShelfItems)
                .offShelfItems(offShelfItems)
                .lowStockItems(lowStockItems)
                .totalCategories(totalCategories)
                .categoryDistribution(categoryDist)
                .topSellingItems(topSelling)
                .build();
    }

    private List<ItemSku> buildSkuList(Long itemId, List<ItemSkuDTO> skus) {
        if (CollUtils.isEmpty(skus)) {
            return Collections.emptyList();
        }
        return skus.stream().map(sku -> {
            ItemSku itemSku = new ItemSku();
            itemSku.setItemId(itemId);
            itemSku.setPrice(sku.getPrice());
            itemSku.setStock(sku.getStock());
            itemSku.setImage(sku.getImage());
            itemSku.setSpecs(JSON.toJSONString(sku.getSpecs()));
            return itemSku;
        }).collect(Collectors.toList());
    }

    private int calcTotalSkuStock(List<ItemSkuDTO> skus) {
        if (CollUtils.isEmpty(skus)) {
            return 0;
        }
        return skus.stream()
                .map(ItemSkuDTO::getStock)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
    }

    private void checkItemDTO(ItemDTO itemDTO, boolean update) {
        if (itemDTO == null) {
            throw new BusinessException("商品参数不能为空");
        }
        if (update && itemDTO.getId() == null) {
            throw new BusinessException("商品ID不能为空");
        }
        if (CollUtils.isEmpty(itemDTO.getSkus())) {
            throw new BusinessException("SKU不能为空");
        }
    }

    private ItemDetailVO convertDetail(Item item, ItemDetail itemDetail, List<ItemSku> skuList) {
        ItemDetailVO vo = new ItemDetailVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setSubTitle(item.getSubTitle());
        vo.setPrice(item.getPrice());
        vo.setOriginalPrice(item.getOriginalPrice());
        vo.setTags(item.getTags());
        vo.setSold(item.getSold());
        vo.setAvgScore(item.getAvgScore());
        vo.setStatus(item.getStatus());
        vo.setCategory(item.getCategory());
        vo.setBrand(item.getBrand());
        if (itemDetail != null) {
            vo.setBannerImages(parseStringList(itemDetail.getBannerImages()));
            vo.setDetailHtml(itemDetail.getDetailHtml());
            vo.setVideoUrl(itemDetail.getVideoUrl());
            vo.setSpecTemplate(parseSpecTemplate(itemDetail.getSpecTemplate()));
        }
        if (CollUtils.isNotEmpty(skuList)) {
            List<ItemDetailVO.SkuVO> skuVOS = skuList.stream().map(sku -> {
                ItemDetailVO.SkuVO skuVO = new ItemDetailVO.SkuVO();
                skuVO.setId(sku.getId());
                skuVO.setPrice(sku.getPrice());
                skuVO.setStock(sku.getStock());
                skuVO.setImage(sku.getImage());
                skuVO.setSpecs(parseSpecMap(sku.getSpecs()));
                return skuVO;
            }).collect(Collectors.toList());
            vo.setSkuList(skuVOS);
        }
        return vo;
    }

    private List<String> parseStringList(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JSON.parseArray(json, String.class);
    }

    private List<ItemDetailVO.SpecTemplateItem> parseSpecTemplate(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JSON.parseArray(json, ItemDetailVO.SpecTemplateItem.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseSpecMap(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JSON.parseObject(json, Map.class);
    }
}
