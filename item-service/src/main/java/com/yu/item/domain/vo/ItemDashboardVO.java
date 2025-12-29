package com.yu.item.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@ApiModel(description = "商品管理看板数据VO")
public class ItemDashboardVO implements Serializable {

    @ApiModelProperty("商品总数")
    private Long totalItems;

    @ApiModelProperty("上架商品数")
    private Long onShelfItems;

    @ApiModelProperty("下架商品数")
    private Long offShelfItems;

    @ApiModelProperty("库存紧张商品数(库存<10)")
    private Long lowStockItems;

    @ApiModelProperty("分类总数")
    private Long totalCategories;

    @ApiModelProperty("商品分类分布（key:分类名, value:商品数）")
    private List<ChartDataVO> categoryDistribution;

    @ApiModelProperty("商品销量排行Top10（key:商品名, value:销量）")
    private List<ChartDataVO> topSellingItems;

    @Data
    @Builder
    @ApiModel("图表数据项")
    public static class ChartDataVO implements Serializable {
        @ApiModelProperty("名称")
        private String name;
        @ApiModelProperty("数值")
        private Object value;
    }
}