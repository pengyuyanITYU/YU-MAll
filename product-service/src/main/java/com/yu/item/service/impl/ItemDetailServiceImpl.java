package com.yu.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.item.domain.po.ItemDetail;
import com.yu.item.mapper.ItemDetailMapper;
import com.yu.item.service.IItemDetailService;
import org.springframework.stereotype.Service;

@Service
public class ItemDetailServiceImpl extends ServiceImpl<ItemDetailMapper, ItemDetail> implements IItemDetailService {
}
