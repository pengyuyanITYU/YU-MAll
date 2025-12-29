package com.yu.item.domain.po;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("category")
// EasyExcel 样式配置
@HeadRowHeight(20)      // 表头高度
@ContentRowHeight(18)   // 内容高度
@ColumnWidth(20)        // 默认列宽
public class Category {

    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    @ExcelProperty(value = "分类ID", index = 0)
    private Long id;

    /**
     * 分类名称
     */
    @ExcelProperty(value = "分类名称", index = 1)
    @ColumnWidth(30) // 名称通常较长，单独加宽
    private String name;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty(value = "创建时间", index = 2)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(25)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ExcelProperty(value = "更新时间", index = 3)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(25)
    private LocalDateTime updateTime;
}