package com.yu.order.domain.po;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.baomidou.mybatisplus.annotation.*;
import com.yu.common.convertor.OrderStatusConverter;
import com.yu.common.convertor.PaymentTypeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("`order`")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单id (雪花算法，必须转 String 防止科学计数法)
     * 宽度 22：足够容纳 19 位 ID
     */
    @ColumnWidth(22)
    @ExcelProperty(value = "订单号", converter = LongStringConverter.class)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     * 宽度 22：同上
     */
    @ColumnWidth(22)
    @ExcelProperty(value = "用户ID", converter = LongStringConverter.class)
    private Long userId;

    /**
     * 总金额，单位为分
     * 宽度 15：金额不需要太宽
     */
    @ColumnWidth(15)
    @ExcelProperty("总金额(分)")
    private Long totalFee;

    /**
     * 支付类型：1、支付宝，2、微信，3、扣减余额
     * 使用自定义 Converter 自动转中文
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "支付方式", converter = PaymentTypeConverter.class)
    private Integer paymentType;

    /**
     * 订单状态
     * 使用自定义 Converter 自动转中文
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "订单状态", converter = OrderStatusConverter.class)
    private Integer status;

    // ==========================================
    // 地址快照
    // ==========================================

    /**
     * 地址薄ID
     * 业务导出通常不需要看这个 ID，如果不重要可以加 @ExcelIgnore
     */
    @ExcelIgnore
    private Long addressId;

    /**
     * 收货人姓名
     * 宽度 15：中文名一般 2-4 字，15 足够
     */
    @ColumnWidth(15)
    @ExcelProperty("收货人")
    private String receiverContact;

    /**
     * 收货人手机号
     * 宽度 18：手机号 11 位，留点余量
     */
    @ColumnWidth(18)
    @ExcelProperty("手机号")
    private String receiverMobile;

    /**
     * 收货详细地址
     * 宽度 50：地址通常很长，给大一点
     */
    @ColumnWidth(50)
    @ExcelProperty("收货地址")
    private String receiverAddress;

    // ==========================================
    // 时间字段
    // 统一宽度 22 + 格式化，视觉上非常整齐
    // ==========================================

    @TableField(fill = FieldFill.INSERT)
    @ColumnWidth(22)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ColumnWidth(22)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("支付时间")
    private LocalDateTime payTime;

    @ColumnWidth(22)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("发货时间")
    private LocalDateTime consignTime;

    @ColumnWidth(22)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("确认收货时间")
    private LocalDateTime endTime;

    @ColumnWidth(22)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("关闭时间")
    private LocalDateTime closeTime;

    @ColumnWidth(22)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("评价时间")
    private LocalDateTime commentTime;

    @ColumnWidth(22)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}