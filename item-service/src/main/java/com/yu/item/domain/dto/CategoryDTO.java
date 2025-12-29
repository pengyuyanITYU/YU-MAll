package com.yu.item.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
@ApiModel(description = "商品分类DTO")
public class CategoryDTO {

    /**
     * 分类ID（更新时需要）
     */
    @ApiModelProperty(value = "分类ID", example = "1")
    private Long id;

    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称", example = "电子产品")
    @NotBlank(message = "分类名称不能为空")
    private String name;
}