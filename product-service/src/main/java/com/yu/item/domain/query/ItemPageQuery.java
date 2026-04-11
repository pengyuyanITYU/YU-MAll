package com.yu.item.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "商品分页查询参数")
public class ItemPageQuery extends PageQuery {

    @ApiModelProperty("搜索关键字")
    private String name;

    @ApiModelProperty("商品分类")
    private String category;

    @ApiModelProperty("商品品牌")
    private String brand;

    @ApiModelProperty("店铺ID")
    private Long shopId;

    @ApiModelProperty("销量排序标记")
    private String sold;

    @ApiModelProperty("最低价")
    private Integer minPrice;

    @ApiModelProperty("最高价")
    private Integer maxPrice;
}
