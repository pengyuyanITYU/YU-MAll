package com.yu.item.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;


@Data
@TableName(value="item_detail",autoResultMap = true)
@Accessors(chain = true)
public class ItemDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;

    // JSON字段在PO中建议先用String接收，或配合MyBatis TypeHandler
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> bannerImages; // ["url1", "url2"]
    private String detailHtml;   // 富文本
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<SpecTemplate> specTemplate; // [{"name":"颜色","values":["红","黑"]}]
    private String videoUrl;
}