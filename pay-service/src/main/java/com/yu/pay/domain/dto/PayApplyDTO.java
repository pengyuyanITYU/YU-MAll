package com.yu.pay.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@ApiModel(description = "支付下单参数")
public class PayApplyDTO {

    @ApiModelProperty("业务订单号")
    @NotNull(message = "业务订单号不能为空")
    private Long bizOrderNo;

    @ApiModelProperty("支付金额")
    @Min(value = 1, message = "支付金额必须大于0")
    private Long amount;

    @ApiModelProperty("支付渠道编码")
    @NotNull(message = "支付渠道编码不能为空")
    private String payChannelCode;

    @ApiModelProperty("支付方式")
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    @ApiModelProperty("订单描述")
    @NotNull(message = "订单描述不能为空")
    private String orderInfo;
}