package com.yu.item.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@ApiModel(value = "ItemSkuDTO", description = "商品SKU信息")
public class ItemSkuDTO {

    @ApiModelProperty(value = "SKU ID")
    private Long id;

    @NotNull(message = "SKU规格不能为空")
    @ApiModelProperty(value = "SKU规格", required = true)
    private Map<String, String> specs;

    @NotNull(message = "SKU价格不能为空")
    @Min(value = 0, message = "SKU价格不能小于0")
    @ApiModelProperty(value = "SKU价格(分)", required = true)
    private Long price;

    @NotNull(message = "SKU库存不能为空")
    @Min(value = 0, message = "SKU库存不能小于0")
    @ApiModelProperty(value = "SKU库存", required = true)
    private Integer stock;

    @ApiModelProperty(value = "SKU图片")
    private String image;
}
