package com.yu.item.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@ApiModel(description = "商品分类DTO")
public class CategoryDTO {

    @ApiModelProperty(value = "分类ID")
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @ApiModelProperty(value = "分类名称", required = true)
    private String name;
}
