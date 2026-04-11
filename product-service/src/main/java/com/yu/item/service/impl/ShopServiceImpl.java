package com.yu.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.BusinessException;
import com.yu.item.domain.dto.ShopDTO;
import com.yu.item.domain.po.Shop;
import com.yu.item.domain.query.ShopPageQuery;
import com.yu.item.domain.vo.ShopVO;
import com.yu.item.mapper.ShopMapper;
import com.yu.item.service.IShopService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addShop(ShopDTO shopDTO) {
        validateShippingRule(shopDTO);
        Shop shop = new Shop();
        BeanUtils.copyProperties(shopDTO, shop);
        return save(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateShop(ShopDTO shopDTO) {
        if (shopDTO == null || shopDTO.getId() == null || shopDTO.getId() <= 0) {
            throw new BusinessException("店铺ID不合法");
        }
        validateShippingRule(shopDTO);
        Shop shop = getById(shopDTO.getId());
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        BeanUtils.copyProperties(shopDTO, shop);
        return updateById(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteShop(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("店铺ID不合法");
        }
        return removeById(id);
    }

    @Override
    public ShopVO getShopById(Long id) {
        Shop shop = getById(id);
        if (shop == null) {
            return null;
        }
        return toVO(shop);
    }

    @Override
    public ShopVO getEnabledShopById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("店铺ID不合法");
        }
        Shop shop = lambdaQuery()
                .eq(Shop::getId, id)
                .eq(Shop::getStatus, 1)
                .one();
        if (shop == null) {
            return null;
        }
        return toVO(shop);
    }

    @Override
    public TableDataInfo listShops(ShopPageQuery query) {
        ShopPageQuery realQuery = query == null ? new ShopPageQuery() : query;
        Page<Shop> page = new Page<>(realQuery.getPageNo(), realQuery.getPageSize());
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(realQuery.getName()), Shop::getName, realQuery.getName())
                .eq(realQuery.getStatus() != null, Shop::getStatus, realQuery.getStatus())
                .orderByDesc(Shop::getUpdateTime);
        Page<Shop> result = page(page, wrapper);
        List<ShopVO> rows = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return TableDataInfo.success(rows, result.getTotal());
    }

    @Override
    public List<ShopVO> listSimple() {
        return lambdaQuery()
                .eq(Shop::getStatus, 1)
                .orderByAsc(Shop::getName)
                .list()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private ShopVO toVO(Shop shop) {
        ShopVO shopVO = new ShopVO();
        BeanUtils.copyProperties(shop, shopVO);
        shopVO.setShippingDesc(buildShippingDesc(shop));
        return shopVO;
    }

    private String buildShippingDesc(Shop shop) {
        if (shop == null || StrUtil.isBlank(shop.getShippingType())) {
            return "";
        }
        if ("FREE".equals(shop.getShippingType())) {
            return "包邮";
        }
        if ("FIXED".equals(shop.getShippingType())) {
            return "运费" + formatFen(shop.getShippingFee()) + "元";
        }
        if ("THRESHOLD_FREE".equals(shop.getShippingType())) {
            return "满" + formatFen(shop.getFreeShippingThreshold()) + "元包邮";
        }
        return "";
    }

    private String formatFen(Integer amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%.2f", amount / 100.0);
    }

    private void validateShippingRule(ShopDTO shopDTO) {
        if (shopDTO == null) {
            throw new BusinessException("店铺信息不能为空");
        }
        if ("FREE".equals(shopDTO.getShippingType())) {
            shopDTO.setShippingFee(0);
            shopDTO.setFreeShippingThreshold(0);
            return;
        }
        if ("FIXED".equals(shopDTO.getShippingType())) {
            shopDTO.setFreeShippingThreshold(0);
            return;
        }
        if ("THRESHOLD_FREE".equals(shopDTO.getShippingType())) {
            if (shopDTO.getFreeShippingThreshold() == null || shopDTO.getFreeShippingThreshold() <= 0) {
                throw new BusinessException("满额包邮门槛不能为空");
            }
            return;
        }
        throw new BusinessException("不支持的运费模式");
    }
}
