package com.yu.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.api.dto.OrderDetailDTO;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.ItemDTO;
import com.yu.item.domain.po.Item;
import com.yu.item.domain.query.ItemPageQuery;
import com.yu.item.domain.vo.ItemDashboardVO;
import com.yu.item.domain.vo.ItemDetailVO;

import java.util.List;

public interface IItemService extends IService<Item> {

    TableDataInfo listItem(ItemPageQuery itemPageQuery);

    TableDataInfo listAdminItems(ItemPageQuery itemPageQuery);

    ItemDetailVO getItemById(Long id);

    ItemDetailVO getItemBySkuId(Long skuId);

    List<ItemDetailVO> getItemByIds(List<Long> ids);

    void updateStockAndSold(List<OrderDetailDTO> orderDetailDTOList);

    void add(ItemDTO itemDTO);

    void updateItemById(ItemDTO itemDTO);

    ItemDashboardVO getItemDashboardData();
}
