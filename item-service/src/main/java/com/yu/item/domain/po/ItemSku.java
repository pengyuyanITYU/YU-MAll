package com.yu.item.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Map;

@Data
@TableName(value = "item_sku",autoResultMap = true)
public class ItemSku {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;

    // {"颜色": "午夜色", "版本": "41mm GPS版"}
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> specs;

    private Long price;
    private Integer stock;
    private String image; // 该SKU对应的特定图片
}