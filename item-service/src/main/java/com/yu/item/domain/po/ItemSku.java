package com.yu.item.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("item_sku")
public class ItemSku {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;

    private String specs;

    private Long price;

    private Integer stock;

    private String image;
}
