package com.yu.sale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.sale.domain.po.PayOrder;
import com.yu.sale.domain.vo.SaleDashboardVO;
import com.yu.sale.enums.PayStatus;
import com.yu.sale.enums.PayType;
import com.yu.sale.mapper.PayOrderMapper;
import com.yu.sale.service.IPayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {

    private final PayOrderMapper payOrderMapper;

    @Override
    public SaleDashboardVO getSaleDashboardData() {
        // 1. 准备时间范围：今天
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        // 准备时间范围：近7天
        LocalDateTime last7DaysStart = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);

        // 2. 查询概览数据
        // 2.1 总销售额
        Long totalAmount = payOrderMapper.selectTotalSalesAmount();

        // 2.2 总订单数 (支付成功的)
        long totalCount = this.count(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getStatus, PayStatus.TRADE_SUCCESS.getValue()));

        // 2.3 今日销售额
        Long todayAmount = payOrderMapper.selectSalesAmountByTime(todayStart, todayEnd);

        // 2.4 今日订单数
        long todayCount = this.count(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getStatus, PayStatus.TRADE_SUCCESS.getValue())
                .ge(PayOrder::getPaySuccessTime, todayStart)
                .le(PayOrder::getPaySuccessTime, todayEnd));

        // 3. 查询趋势图数据 (近7天)
        List<Map<String, Object>> trendMapList = payOrderMapper.selectSalesTrend(last7DaysStart);
        // 转换格式，确保日期连续（如果某天没数据需要补0，这里简单处理，前端可处理或在此处补全）
        List<SaleDashboardVO.ChartDataVO> trendList = trendMapList.stream().map(m -> SaleDashboardVO.ChartDataVO.builder()
                .name((String) m.get("name"))
                .value(m.get("value"))
                .build()).collect(Collectors.toList());

        // 4. 查询支付方式分布
        List<Map<String, Object>> payTypeMapList = payOrderMapper.selectPayTypeStats();
        List<SaleDashboardVO.ChartDataVO> payTypeStats = new ArrayList<>();

        // 枚举转换：将数据库的 1,2,3 转换为 "支付宝", "微信" 等中文描述
        for (Map<String, Object> m : payTypeMapList) {
            Integer typeVal = (Integer) m.get("name"); // 数据库查出来的是 Integer
            String typeName = "未知";
            if (typeVal != null) {
                // 简单的遍历匹配，也可以在 PayType 中加个 getDescByValue 静态方法
                for (PayType pt : PayType.values()) {
                    if (pt.getValue() == typeVal) {
                        typeName = pt.getDesc();
                        break;
                    }
                }
            }
            payTypeStats.add(SaleDashboardVO.ChartDataVO.builder()
                    .name(typeName)
                    .value(m.get("value"))
                    .build());
        }

        // 5. 组装返回
        return SaleDashboardVO.builder()
                .totalSalesAmount(totalAmount)
                .totalOrderCount(totalCount)
                .todaySalesAmount(todayAmount)
                .todayOrderCount(todayCount)
                .salesTrend(trendList)
                .payTypeStats(payTypeStats)
                .build();
    }
}