package com.yu.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.common.constant.HttpStatus;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.BusinessException;
import com.yu.common.utils.BeanUtils;
import com.yu.common.utils.CollUtils;
import com.yu.item.domain.dto.ItemDTO;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl extends  ServiceImpl<ItemMapper, Item> implements IItemService{

    private final ItemMapper itemMapper;
    private final ItemDetailMapper itemDetailMapper;
    private final ItemSkuMapper itemSkuMapper;

    private final IItemDetailService itemDetailService;

    private final IItemSkuService itemSkuService;

    private ICategoryService categoryService;

    @Autowired
    @Lazy // 【关键】：告诉 Spring，这个依赖稍后用到再加载，别在启动时卡死
    public void setCategoryService(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @Override
    public TableDataInfo listItem(ItemPageQuery itemPageQuery) {
        Page<Item> p = new Page<>(itemPageQuery.getPageNo(), itemPageQuery.getPageSize());
        Page<Item> result = lambdaQuery().eq(Item::getStatus, 1)

                .like(StrUtil.isNotBlank(itemPageQuery.getName()), Item::getName, itemPageQuery.getName())
                .eq(StrUtil.isNotBlank(itemPageQuery.getCategory()), Item::getCategory, itemPageQuery.getCategory())
                .eq(StrUtil.isNotBlank(itemPageQuery.getBrand()), Item::getBrand, itemPageQuery.getBrand())
                .ge(itemPageQuery.getMinPrice() != null, Item::getPrice, itemPageQuery.getMinPrice())
                .le(itemPageQuery.getMaxPrice() != null, Item::getPrice, itemPageQuery.getMaxPrice())
                .orderByDesc(Item::getUpdateTime)
                .page(p);
        TableDataInfo tableDataInfo = new TableDataInfo();
        tableDataInfo.setCode(HttpStatus.SUCCESS);
        tableDataInfo.setMsg("查询成功");
        tableDataInfo.setRows(result.getRecords());
        tableDataInfo.setTotal(result.getTotal());
        return tableDataInfo;}


    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void add(ItemDTO itemDTO) {
        if (itemDTO == null  ||CollUtils.isEmpty(itemDTO.getSkus())){
            log.error("商品信息不能为空");
            throw new BusinessException("商品信息不能为空");
        }
        Item item = new Item();
        item.setName(itemDTO.getName())
            .setPrice(itemDTO.getPrice())
            .setImage(itemDTO.getImage())
            .setStock(itemDTO.getStock())
            .setCategory(itemDTO.getCategory())
            .setTags(itemDTO.getTags())
            .setBrand(itemDTO.getBrand())
            .setStatus(itemDTO.getStatus())
            .setSubTitle(itemDTO.getSubTitle())
            .setCategoryId(itemDTO.getCategoryId())
            .setOriginalPrice(itemDTO.getOriginalPrice());
        boolean save = save(item);
        if (!save) {
            log.error("商品添加失败");
            throw new BusinessException("商品添加失败");
        }
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setItemId(item.getId())
                  .setBannerImages(itemDTO.getBannerImages())
                  .setDetailHtml(itemDTO.getDetailHtml())
                  .setVideoUrl(itemDTO.getVideoUrl())
                  .setSpecTemplate(itemDTO.getSpecTemplate());
        boolean save1 = itemDetailService.save(itemDetail);
        if (!save1) {
            log.error("商品详情添加失败");
            throw new BusinessException("商品详情添加失败");
        }
        List<ItemSku> itemSkuList = itemDTO.getSkus().stream().map(sku -> {
            ItemSku itemSku = new ItemSku();
            BeanUtils.copyProperties(sku, itemSku);
            itemSku.setItemId(item.getId());
            return itemSku;
        }).collect(Collectors.toList());
        boolean save2 = itemSkuService.saveBatch(itemSkuList);
        if (!save2) {
            log.error("商品SKU添加失败");
            throw new BusinessException("商品sku添加失败");
        }
        log.info("商品添加成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void updateItemById(ItemDTO itemDTO) {
        Item item = BeanUtils.copyProperties(itemDTO,Item.class);
        boolean itemUpdate = updateById(item);
        if (!itemUpdate) {
            log.error("商品更新失败");
            throw new BusinessException("商品更新失败");
        }
        ItemDetail itemDetail = itemDetailService.getById(item.getId());
        itemDetail.setBannerImages(itemDTO.getBannerImages())
                  .setDetailHtml(itemDTO.getDetailHtml())
                  .setVideoUrl(itemDTO.getVideoUrl())
                  .setSpecTemplate(itemDTO.getSpecTemplate());
        if(item.getId() == null){
            log.warn("商品ID不能为空");
            throw new BusinessException("商品ID不能为空");
        }
        boolean itemDetailUpdate = itemDetailService.updateById(itemDetail);
        if(!itemDetailUpdate){
            log.error("商品详情更新失败");
            throw new BusinessException("商品详情更新失败");
        }
        List<ItemSku> itemSkuList = itemDTO.getSkus().stream().map(sku -> {
            ItemSku itemSku = new ItemSku();
            BeanUtils.copyProperties(sku, itemSku);
            itemSku.setItemId(item.getId());
            return itemSku;
        }).collect(Collectors.toList());
        boolean itemSkuUpdate = itemSkuService.updateBatchById(itemSkuList);
        if (!itemSkuUpdate) {
            log.error("商品SKU更新失败");
            throw new BusinessException("商品SKU更新失败");
        }
        log.info("商品更新成功");
    }

    @Override
    public ItemDetailVO getItemById(Long id) {
        // 1. 查询商品基本信息
        Item item = itemMapper.selectById(id);
        if (item == null) {
            return null;
        }
        
        // 2. 查询商品详情信息
        LambdaQueryWrapper<ItemDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(ItemDetail::getItemId, id);
        ItemDetail itemDetail = itemDetailMapper.selectOne(detailWrapper);
        
        // 3. 查询商品SKU列表
        LambdaQueryWrapper<ItemSku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(ItemSku::getItemId, id);
        List<ItemSku> skuList = itemSkuMapper.selectList(skuWrapper);
        
        // 4. 组装ItemDetailVO
        ItemDetailVO itemDetailVO = new ItemDetailVO();
        
        // 设置商品基本信息
        itemDetailVO.setId(item.getId());
        itemDetailVO.setName(item.getName());
        itemDetailVO.setSubTitle(item.getSubTitle());
        itemDetailVO.setPrice(item.getPrice());
        itemDetailVO.setOriginalPrice(item.getOriginalPrice());
        itemDetailVO.setTags(item.getTags());
        itemDetailVO.setSold(item.getSold());
        itemDetailVO.setStatus(item.getStatus());
        itemDetailVO.setBrand(item.getBrand());
        itemDetailVO.setCategory(item.getCategory());
        itemDetailVO.setAvgScore(item.getAvgScore());
        
        // 设置商品详情信息
        if (itemDetail != null) {
            // 将JSON字符串转换为Java对象
            if (itemDetail.getBannerImages() != null) {
                itemDetailVO.setBannerImages(itemDetail.getBannerImages());
            }
            itemDetailVO.setDetailHtml(itemDetail.getDetailHtml());
            itemDetailVO.setVideoUrl(itemDetail.getVideoUrl());
            
            // 解析规格模板
            if (itemDetail.getSpecTemplate() != null) {
                itemDetailVO.setSpecTemplate(itemDetail.getSpecTemplate());
            }
        }
        
        // 设置SKU列表
        if (!skuList.isEmpty()) {
            List<ItemDetailVO.SkuVO> skuVOS = skuList.stream().map(sku -> {
                ItemDetailVO.SkuVO skuVO = new ItemDetailVO.SkuVO();
                skuVO.setId(sku.getId());
                skuVO.setPrice(sku.getPrice());
                skuVO.setStock(sku.getStock());
                skuVO.setImage(sku.getImage());
                
                // 将规格JSON字符串转换为Map
                if (sku.getSpecs() != null) {
                    skuVO.setSpecs(sku.getSpecs());
                }
                
                return skuVO;
            }).collect(java.util.stream.Collectors.toList());
            
            itemDetailVO.setSkuList(skuVOS);
        }
        
        return itemDetailVO;
    }


    @Override
    public List<ItemDetailVO> getItemByIds(List<Long> ids) {
        // 0. 判空处理
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 批量查询商品基本信息
        List<Item> items = listByIds(ids);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // 确保后续查询只查存在的ID
        List<Long> validIds = items.stream().map(Item::getId).collect(Collectors.toList());

        // 2. 批量查询商品详情信息 (WHERE item_id IN (...))
        LambdaQueryWrapper<ItemDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(ItemDetail::getItemId, validIds);
        List<ItemDetail> detailList = itemDetailMapper.selectList(detailWrapper);

        // 转为 Map<ItemId, ItemDetail> 方便后续 O(1) 获取
        Map<Long, ItemDetail> detailMap = detailList.stream()
                .collect(Collectors.toMap(ItemDetail::getItemId, Function.identity()));

        // 3. 批量查询商品SKU列表 (WHERE item_id IN (...))
        LambdaQueryWrapper<ItemSku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.in(ItemSku::getItemId, validIds);
        List<ItemSku> allSkuList = itemSkuMapper.selectList(skuWrapper);

        // 转为 Map<ItemId, List<ItemSku>> 进行分组
        Map<Long, List<ItemSku>> skuMap = allSkuList.stream()
                .collect(Collectors.groupingBy(ItemSku::getItemId));

        // 4. 组装结果 List
        return items.stream().map(item -> {
            ItemDetailVO itemDetailVO = new ItemDetailVO();

            // --- A. 设置商品基本信息 ---
            // 可以使用 BeanUtils 简化，也可以像参考代码一样手动 set
            itemDetailVO.setId(item.getId());
            itemDetailVO.setName(item.getName());
            itemDetailVO.setSubTitle(item.getSubTitle());
            itemDetailVO.setPrice(item.getPrice());
            itemDetailVO.setOriginalPrice(item.getOriginalPrice());
            itemDetailVO.setTags(item.getTags());
            itemDetailVO.setSold(item.getSold());
            itemDetailVO.setAvgScore(item.getAvgScore());

            // --- B. 设置商品详情信息 ---
            ItemDetail itemDetail = detailMap.get(item.getId());
            if (itemDetail != null) {
                if (itemDetail.getBannerImages() != null) {
                    itemDetailVO.setBannerImages(itemDetail.getBannerImages());
                }
                itemDetailVO.setDetailHtml(itemDetail.getDetailHtml());
                itemDetailVO.setVideoUrl(itemDetail.getVideoUrl());

                if (itemDetail.getSpecTemplate() != null) {
                    itemDetailVO.setSpecTemplate(itemDetail.getSpecTemplate());
                }
            }

            // --- C. 设置SKU列表 ---
            List<ItemSku> currentItemSkus = skuMap.get(item.getId());
            if (currentItemSkus != null && !currentItemSkus.isEmpty()) {
                List<ItemDetailVO.SkuVO> skuVOS = currentItemSkus.stream().map(sku -> {
                    ItemDetailVO.SkuVO skuVO = new ItemDetailVO.SkuVO();
                    skuVO.setId(sku.getId());
                    skuVO.setPrice(sku.getPrice());
                    skuVO.setStock(sku.getStock());
                    skuVO.setImage(sku.getImage());

                    if (sku.getSpecs() != null) {
                        skuVO.setSpecs(sku.getSpecs());
                    }
                    return skuVO;
                }).collect(Collectors.toList());

                itemDetailVO.setSkuList(skuVOS);
            }

            return itemDetailVO;
        }).collect(Collectors.toList());
    }

    @Override
    public ItemDashboardVO getItemDashboardData() {
        // 1. 基础统计
        Long totalItems = this.count();
        Long onShelfItems = this.count(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 1));
        Long offShelfItems = this.count(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 2));
        Long lowStockItems = itemMapper.countLowStock();
        Long totalCategories = categoryService.count();

        // 2. 统计分类分布
        List<Map<String, Object>> categoryDistMaps = itemMapper.selectCategoryDistribution();
        List<ItemDashboardVO.ChartDataVO> categoryDist = categoryDistMaps.stream()
                .map(m -> ItemDashboardVO.ChartDataVO.builder()
                        .name((String) m.get("name"))
                        .value(m.get("value"))
                        .build())
                .collect(Collectors.toList());

        // 3. 统计热销 Top 10
        List<Map<String, Object>> topSellingMaps = itemMapper.selectTopSelling();
        List<ItemDashboardVO.ChartDataVO> topSelling = topSellingMaps.stream()
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
}
