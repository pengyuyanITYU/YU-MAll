package com.yu.item.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("item_detail")
public class ItemDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;

    private String bannerImages;

    private String detailHtml;

    private String specTemplate;

    private String videoUrl;
}
