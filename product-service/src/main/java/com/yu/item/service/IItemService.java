package com.yu.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.api.dto.OrderDetailDTO;
import com.yu.api.vo.SearchItemVO;
import com.yu.common.domain.query.PageQuery;
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

    TableDataInfo listSearchItems(PageQuery query);

    ItemDetailVO getItemById(Long id);

    SearchItemVO getSearchItemById(Long id);

    ItemDetailVO getItemBySkuId(Long skuId);

    List<ItemDetailVO> getItemByIds(List<Long> ids);

    void updateStockAndSold(List<OrderDetailDTO> orderDetailDTOList);

    void add(ItemDTO itemDTO);

    void updateItemById(ItemDTO itemDTO);

    boolean deleteItemById(Long id);

    boolean deleteItemsByIds(List<Long> ids);

    ItemDashboardVO getItemDashboardData();
}
