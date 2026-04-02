package com.yu.order.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "创建订单表单")
public class OrderFormDTO {

    @ApiModelProperty(value = "收货地址ID", required = true)
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @ApiModelProperty(value = "支付方式：1微信，2支付宝，3余额", required = true)
    @NotNull(message = "支付方式不能为空")
    @Min(value = 1, message = "支付方式非法")
    private Integer paymentType;

    @ApiModelProperty(value = "订单商品明细", required = true)
    @NotEmpty(message = "订单明细不能为空")
    @Valid
    private List<OrderDetailDTO> details;

    @ApiModelProperty(value = "订单幂等Token")
    private String token;

    @NotNull(message = "订单总金额不能为空")
    @ApiModelProperty(value = "订单总金额(分)", required = true)
    private Long totalFee;
}