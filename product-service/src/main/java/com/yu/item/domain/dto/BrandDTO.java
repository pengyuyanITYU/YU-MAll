package com.yu.item.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@ApiModel(description = "商品品牌DTO")
public class BrandDTO {

    @ApiModelProperty(value = "品牌ID")
    private Long id;

    @NotBlank(message = "品牌名称不能为空")
    @ApiModelProperty(value = "品牌名称", required = true)
    private String name;
}
