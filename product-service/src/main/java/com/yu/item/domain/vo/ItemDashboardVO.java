package com.yu.item.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@ApiModel(description = "商品看板VO")
public class ItemDashboardVO implements Serializable {

    @ApiModelProperty("商品总数")
    private Long totalItems;

    @ApiModelProperty("上架商品数")
    private Long onShelfItems;

    @ApiModelProperty("下架商品数")
    private Long offShelfItems;

    @ApiModelProperty("低库存商品数")
    private Long lowStockItems;

    @ApiModelProperty("分类总数")
    private Long totalCategories;

    @ApiModelProperty("分类分布")
    private List<ChartDataVO> categoryDistribution;

    @ApiModelProperty("销量Top")
    private List<ChartDataVO> topSellingItems;

    @Data
    @Builder
    public static class ChartDataVO implements Serializable {
        private String name;
        private Object value;
    }
}
