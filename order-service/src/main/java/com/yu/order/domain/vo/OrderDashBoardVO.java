package com.yu.order.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yu.order.domain.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderDashBoardVO {
    private Long id;
    private String avatar;
    private String nickName;
    private Long totalPrice;
    private OrderStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
