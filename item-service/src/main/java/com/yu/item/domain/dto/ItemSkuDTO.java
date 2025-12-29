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

    @ApiModelProperty(value = "SKU ID (新增时为空，修改时必填)", example = "1001")
    private Long id;

    @ApiModelProperty(value = "销售属性集合，JSON对象格式", example = "{\"颜色\": \"红色\", \"内存\": \"128G\"}", required = true)
    @NotNull(message = "SKU规格属性不能为空")
    private Map<String, String> specs;

    @ApiModelProperty(value = "价格（单位：分）", example = "9900", required = true)
    @NotNull(message = "SKU价格不能为空")
    @Min(value = 0, message = "价格不能小于0")
    private Long price;

    @ApiModelProperty(value = "库存数量", example = "100", required = true)
    @NotNull(message = "SKU库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

    @ApiModelProperty(value = "SKU专属图片地址", example = "http://img.yu.com/sku1.jpg")
    private String image;
}