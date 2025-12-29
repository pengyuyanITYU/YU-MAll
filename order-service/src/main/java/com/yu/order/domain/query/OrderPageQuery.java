package com.yu.order.domain.query;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "商品分页查询参数")
public class OrderPageQuery extends PageQuery {
    @ApiModelProperty("订单状态")
    private Integer status;
    @ApiModelProperty("支付类型")
    private Integer paymentType;
    @ApiModelProperty("用户ID")
    private Long userId;
}
