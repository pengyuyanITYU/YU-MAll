package com.yu.item.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.BizIllegalException;
import com.yu.item.domain.dto.BrandDTO;
import com.yu.item.domain.po.Brand;
import com.yu.item.domain.po.Item;
import com.yu.item.domain.query.BrandQuery;
import com.yu.item.domain.vo.BrandVO;
import com.yu.item.mapper.BrandMapper;
import com.yu.item.service.IBrandService;
import com.yu.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements IBrandService {

    private final @Lazy IItemService itemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBrand(BrandDTO brandDTO) {
        if (brandDTO == null || StrUtil.isBlank(brandDTO.getName())) {
            throw new BizIllegalException("品牌名称不能为空");
        }
        Long count = lambdaQuery().eq(Brand::getName, brandDTO.getName()).count();
        if (count != null && count > 0) {
            throw new BizIllegalException("品牌名称已存在");
        }
        Brand brand = new Brand();
        BeanUtils.copyProperties(brandDTO, brand);
        return save(brand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBrand(Long id) {
        if (id == null || id <= 0) {
            throw new BizIllegalException("品牌ID不合法");
        }
        Brand brand = getById(id);
        if (brand == null) {
            throw new BizIllegalException("品牌不存在");
        }
        if (itemService.lambdaQuery().eq(Item::getBrand, brand.getName()).count() > 0) {
            throw new BizIllegalException("品牌下存在商品，无法删除");
        }
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBrand(BrandDTO brandDTO) {
        if (brandDTO == null || brandDTO.getId() == null || brandDTO.getId() <= 0) {
            throw new BizIllegalException("品牌ID不合法");
        }
        if (StrUtil.isBlank(brandDTO.getName())) {
            throw new BizIllegalException("品牌名称不能为空");
        }
        Brand brand = getById(brandDTO.getId());
        if (brand == null) {
            throw new BizIllegalException("品牌不存在");
        }
        Long count = lambdaQuery()
                .eq(Brand::getName, brandDTO.getName())
                .ne(Brand::getId, brandDTO.getId())
                .count();
        if (count != null && count > 0) {
            throw new BizIllegalException("品牌名称已存在");
        }
        String oldBrandName = brand.getName();
        boolean brandNameChanged = !StrUtil.equals(oldBrandName, brandDTO.getName());
        BeanUtils.copyProperties(brandDTO, brand);
        boolean updated = updateById(brand);
        if (!updated) {
            return false;
        }
        if (!brandNameChanged) {
            return true;
        }
        Long itemCount = itemService.lambdaQuery()
                .eq(Item::getBrand, oldBrandName)
                .count();
        if (itemCount == null || itemCount <= 0) {
            return true;
        }
        return itemService.lambdaUpdate()
                .eq(Item::getBrand, oldBrandName)
                .set(Item::getBrand, brandDTO.getName())
                .update();
    }

    @Override
    public BrandVO getBrandById(Long id) {
        if (id == null || id <= 0) {
            throw new BizIllegalException("品牌ID不合法");
        }
        Brand brand = getById(id);
        if (brand == null) {
            throw new BizIllegalException("品牌不存在");
        }
        BrandVO vo = new BrandVO();
        BeanUtils.copyProperties(brand, vo);
        return vo;
    }

    @Override
    public TableDataInfo getAllBrands(BrandQuery query) {
        BrandQuery realQuery = query == null ? new BrandQuery() : query;
        Page<Brand> page = new Page<>(realQuery.getPageNo(), realQuery.getPageSize());
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(realQuery.getName()), Brand::getName, realQuery.getName());
        Page<Brand> result = page(page, wrapper);
        return TableDataInfo.success(result.getRecords(), result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new BizIllegalException("品牌ID不能为空");
        }
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new BizIllegalException("品牌ID不合法");
            }
            Brand brand = getById(id);
            if (brand == null) {
                throw new BizIllegalException("品牌不存在");
            }
            if (itemService.lambdaQuery().eq(Item::getBrand, brand.getName()).count() > 0) {
                throw new BizIllegalException("品牌下存在商品，无法删除");
            }
        }
        return removeByIds(ids);
    }
}
