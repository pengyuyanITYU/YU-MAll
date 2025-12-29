package com.yu.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.sale.domain.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {

    /**
     * 统计指定时间范围内的销售额趋势
     * @param beginTime 开始时间
     * @return map list (date, amount)
     */
    @Select("SELECT DATE_FORMAT(pay_success_time, '%Y-%m-%d') as name, SUM(amount) as value " +
            "FROM pay_order " +
            "WHERE status = 3 AND pay_success_time >= #{beginTime} " +
            "GROUP BY DATE_FORMAT(pay_success_time, '%Y-%m-%d') " +
            "ORDER BY name")
    List<Map<String, Object>> selectSalesTrend(@Param("beginTime") LocalDateTime beginTime);

    /**
     * 统计支付方式占比
     * @return map list (payType, count)
     */
    @Select("SELECT pay_type as name, COUNT(id) as value " +
            "FROM pay_order " +
            "WHERE status = 3 " +
            "GROUP BY pay_type")
    List<Map<String, Object>> selectPayTypeStats();
    
    /**
     * 统计销售总额（状态为支付成功）
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM pay_order WHERE status = 3")
    Long selectTotalSalesAmount();
    
    /**
     * 统计时间段内销售额
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM pay_order WHERE status = 3 AND pay_success_time BETWEEN #{start} AND #{end}")
    Long selectSalesAmountByTime(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}