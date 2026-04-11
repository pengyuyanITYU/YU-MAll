package com.yu.api.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOrderExportVO {

    private Long id;

    private Long userId;

    private Long totalFee;

    private Integer paymentType;

    private Integer status;

    private String receiverContact;

    private String receiverMobile;

    private String receiverAddress;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
