package com.yu.sale.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@ApiModel(description = "销量看板数据VO")
public class SaleDashboardVO implements Serializable {

    @ApiModelProperty("今日销售额（分）")
    private Long todaySalesAmount;

    @ApiModelProperty("今日订单数")
    private Long todayOrderCount;

    @ApiModelProperty("累计总销售额（分）")
    private Long totalSalesAmount;

    @ApiModelProperty("累计总订单数")
    private Long totalOrderCount;

    @ApiModelProperty("近7天销量趋势（key:日期, value:金额）")
    private List<ChartDataVO> salesTrend;

    @ApiModelProperty("支付方式占比统计")
    private List<ChartDataVO> payTypeStats;

    @Data
    @Builder
    @ApiModel("图表数据项")
    public static class ChartDataVO implements Serializable {
        @ApiModelProperty("名称/日期")
        private String name;
        @ApiModelProperty("数值")
        private Object value;
    }
}