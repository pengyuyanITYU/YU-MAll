package com.yu.api.dto;

import com.yu.api.enums.OrderStatus;
import com.yu.api.enums.PayType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateOrderStatusDTO {

    @NotNull(message = "订单ID不能为空")
    private Long id;

    @NotNull(message = "订单状态不能为空")
    private OrderStatus status;

    @NotNull(message = "支付类型不能为空")
    private PayType paymentType;
}