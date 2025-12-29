package com.yu.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.ItemDTO;
import com.yu.item.domain.po.Item;
import com.yu.item.domain.query.ItemPageQuery;
import com.yu.item.domain.vo.ItemDashboardVO;
import com.yu.item.domain.vo.ItemDetailVO;

import java.util.List;

public interface IItemService extends IService<Item> {


    TableDataInfo listItem(ItemPageQuery itemPageQuery);

    ItemDetailVO getItemById(Long id);

    List<ItemDetailVO> getItemByIds(List<Long> ids);

    void add(ItemDTO itemDTO);

    void updateItemById(ItemDTO itemDTO);


    /**
     * 获取商品管理看板数据
     * @return 看板VO
     */
    ItemDashboardVO getItemDashboardData();
}
