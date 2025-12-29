package com.yu.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 * 实现 Valuable 接口后可以直接这样用：
 * Integer status = OrderStatus.SHIPPED;
 * if (OrderStatus.PAID == status) { ... }
 */
@AllArgsConstructor
@Getter
public enum OrderStatus {

    UNPAID(1, "未付款"),
    PAID(2, "已付款, 未发货"),
    SHIPPED(3, "已发货, 未确认"),
    SUCCESS(4, "交易成功"),
    CANCELED(5, "交易取消"),     // 对应你图里的“交易取消”而不是“交易取消”
    EVALUATED(6, "已评价");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String desc;

    // ========== 常用业务判断方法（极大提升代码可读性）==========
    public boolean isUnpaid()   { return this == UNPAID; }
    public boolean isPaid()     { return this == PAID; }
    public boolean isShipped()  { return this == SHIPPED; }
    public boolean isSuccess()  { return this == SUCCESS; }
    public boolean isCanceled() { return this == CANCELED; }
    public boolean isEvaluated(){ return this == EVALUATED; }


    public static OrderStatus of(Integer value) {
        // 1. 处理空值情况，避免空指针
        if (value == null) {
            return null;
        }

        // 2. 遍历查找
        for (OrderStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }

        // 3. (修改点) 如果找不到对应状态，抛出异常比返回 null 更安全，
        //    这样能快速发现数据库里的脏数据或前端传错的参数。
        throw new IllegalArgumentException("未知的订单状态值: " + value);
    }
    // 是否已支付（包含已发货、交易成功、已评价）
    public boolean isPaidOrLater() {
        return this == PAID || this == SHIPPED || this == SUCCESS || this == EVALUATED;
    }

    // 是否交易完成（成功 or 取消）
    public boolean isFinished() {
        return this == SUCCESS || this == CANCELED;
    }
}