package com.yu.dashBoard.domain.vo;

import com.yu.api.po.Order;
import com.yu.api.vo.ItemDashboardVO;
import com.yu.api.vo.OrderDashBoardVO;
import com.yu.api.vo.SaleDashboardVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@ApiModel(description = "管理后台仪表盘聚合数据")
public class DashboardVO implements Serializable {

    @ApiModelProperty(value = "销量统计模块", notes = "包含今日销售额、趋势图、支付方式占比等")
    private SaleDashboardVO saleDashboard;

    @ApiModelProperty(value = "商品统计模块", notes = "包含商品总数、分类分布、库存预警等")
    private ItemDashboardVO itemDashboard;

    @ApiModelProperty(value = "最新实时订单列表", notes = "通常展示最近生成的5-10条订单记录，用于实时监控")
    private List<OrderDashBoardVO> recentOrders;
}