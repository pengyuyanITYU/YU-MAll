package com.yu.item.domain.po;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
// EasyExcel 样式注解
@HeadRowHeight(20)      // 表头高度
@ContentRowHeight(18)   // 内容高度
@ColumnWidth(20)        // 默认列宽
public class Item {

    @TableId(type = IdType.AUTO)
    @ExcelProperty(value = "商品ID", index = 0)
    private Long id;

    // 基础信息
    @ExcelProperty(value = "商品名称", index = 1)
    @ColumnWidth(30)
    private String name;

    @ExcelProperty(value = "副标题", index = 2)
    @ColumnWidth(30)
    private String subTitle; // 副标题

    @ExcelProperty(value = "分类名称", index = 3)
    private String category;

    @ExcelProperty(value = "品牌", index = 4)
    private String brand;

    @ExcelProperty(value = "价格(分)", index = 5)
    private Long price;

    @ExcelProperty(value = "原价(分)", index = 6)
    private Integer originalPrice; // 原价

    // 库存与销量
    @ExcelProperty(value = "库存", index = 7)
    private Integer stock;

    @ExcelProperty(value = "销量", index = 8)
    private Integer sold;

    // 状态与标签
    @ExcelProperty(value = "状态(1上2下)", index = 9)
    private Integer status; // 1上架 2下架

    @ExcelProperty(value = "评分", index = 10)
    private BigDecimal avgScore;

    @ExcelProperty(value = "评论数", index = 11)
    private Integer commentCount;

    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty(value = "创建时间", index = 12)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(25)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ExcelProperty(value = "更新时间", index = 13)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(25)
    private LocalDateTime updateTime;

    // 通常图片链接很长，放在最后，或者选择忽略不导出
    @ExcelProperty(value = "图片URL", index = 14)
    @ColumnWidth(50)
    private String image;    // 列表图

    // 如果不想导出某个字段，可以使用 @ExcelIgnore
    @ExcelProperty(value = "标签", index = 15)
    private String tags;   // "包邮,满减"

    // 数据库字段，导出时可能不需要ID，看需求决定是否添加 @ExcelIgnore
    @ExcelIgnore
    private Long categoryId;
}