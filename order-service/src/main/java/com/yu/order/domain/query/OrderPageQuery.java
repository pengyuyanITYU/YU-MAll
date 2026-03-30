package com.yu.order.domain.query;

import com.yu.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "Order page query")
public class OrderPageQuery extends PageQuery {
    @ApiModelProperty("order status")
    private Integer status;

    @ApiModelProperty("pay type")
    private Integer paymentType;

    @ApiModelProperty("user id")
    private Long userId;
}
