package com.yu.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.ShopDTO;
import com.yu.item.domain.po.Shop;
import com.yu.item.domain.query.ShopPageQuery;
import com.yu.item.domain.vo.ShopVO;

import java.util.List;

public interface IShopService extends IService<Shop> {

    boolean addShop(ShopDTO shopDTO);

    boolean updateShop(ShopDTO shopDTO);

    boolean deleteShop(Long id);

    ShopVO getShopById(Long id);

    TableDataInfo listShops(ShopPageQuery query);

    List<ShopVO> listSimple();
}
