package com.yu.order.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "Order detail view object")
@Accessors(chain = true)
public class OrderVO {

    @ApiModelProperty("order id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("user id")
    private Long userId;

    @ApiModelProperty("total fee")
    private Long totalFee;

    @ApiModelProperty("payment type")
    private Integer paymentType;

    @ApiModelProperty("order status")
    private Integer status;

    @ApiModelProperty("create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("pay time")
    private LocalDateTime payTime;

    @ApiModelProperty("consign time")
    private LocalDateTime consignTime;

    @ApiModelProperty("end time")
    private LocalDateTime endTime;

    @ApiModelProperty("user nickname")
    private String nickName;

    @ApiModelProperty("receiver contact")
    private String receiverContact;

    @ApiModelProperty("receiver mobile")
    private String receiverMobile;

    @ApiModelProperty("receiver address")
    private String receiverAddress;

    @ApiModelProperty("details")
    private List<OrderDetailVO> details;

    @ApiModelProperty("是否已全部评价")
    private boolean commented;

    @ApiModelProperty("close time")
    private LocalDateTime closeTime;
}
