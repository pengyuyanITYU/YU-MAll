package com.yu.sale.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
@ApiModel(description = "支付确认表单实体")
public class PayOrderFormDTO {

    @ApiModelProperty(value = "业务订单id", required = true)
    @NotNull(message = "业务订单id不能为空")
    private Long bizOrderNo;

    @ApiModelProperty("支付密码")
    @NotNull(message = "支付密码")
    private String password;

    @ApiModelProperty("支付方式")
    @NotNull(message = "支付方式")
    private Integer payType;  // 1、支付宝，2、微信，3、扣减余额

    @ApiModelProperty("支付金额")
    @NotNull(message = "支付金额")
    private Long amount;

}