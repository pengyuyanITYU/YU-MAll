package com.yu.item.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item")
@EqualsAndHashCode(callSuper = false)
public class Item {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String subTitle;

    private String image;

    private Long price;

    private Integer originalPrice;

    private Integer stock;

    private Integer sold;

    private Integer commentCount;

    private BigDecimal avgScore;

    private String tags;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String category;

    private String brand;

    private Long categoryId;

    private Long shopId;
}
