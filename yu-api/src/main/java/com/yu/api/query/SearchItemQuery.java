package com.yu.api.query;

import com.yu.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "商品搜索条件")
public class SearchItemQuery extends PageQuery {

    @ApiModelProperty("搜索关键词")
    private String keyword;

    @ApiModelProperty("商品分类")
    private String category;

    @ApiModelProperty("商品品牌")
    private String brand;

    @ApiModelProperty("最低价格")
    private Integer minPrice;

    @ApiModelProperty("最高价格")
    private Integer maxPrice;
}
