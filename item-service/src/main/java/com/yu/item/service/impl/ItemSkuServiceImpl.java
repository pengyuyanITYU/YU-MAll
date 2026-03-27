package com.yu.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.item.domain.po.ItemSku;
import com.yu.item.mapper.ItemSkuMapper;
import com.yu.item.service.IItemSkuService;
import org.springframework.stereotype.Service;

@Service
public class ItemSkuServiceImpl extends ServiceImpl<ItemSkuMapper, ItemSku> implements IItemSkuService {
}
