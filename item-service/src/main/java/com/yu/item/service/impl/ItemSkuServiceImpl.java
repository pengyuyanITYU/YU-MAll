package com.yu.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.item.domain.po.ItemSku;
import com.yu.item.mapper.ItemSkuMapper;
import com.yu.item.service.IItemSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ItemSkuServiceImpl extends ServiceImpl<ItemSkuMapper, ItemSku> implements IItemSkuService {
}
