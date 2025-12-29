package com.yu.api.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import com.yu.api.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderDashBoardVO {

    private Long id;
    private String avatar;
    private String nickName;
    private Long totalPrice;
    private OrderStatus status;
    private LocalDateTime createTime;

}
