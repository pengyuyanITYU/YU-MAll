package com.yu.cart.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "新增购物车商品表单")
public class CartFormDTO {

    @ApiModelProperty("商品ID")
    private Long itemId;

    @ApiModelProperty("商品名称")
    private String name;

    @ApiModelProperty("商品数量")
    private Integer num;

    @ApiModelProperty("商品规格")
    private String spec;

    @ApiModelProperty("商品价格")
    private Long price;

    @ApiModelProperty("商品图片")
    private String image;
}