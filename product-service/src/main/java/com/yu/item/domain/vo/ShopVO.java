package com.yu.item.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShopVO {

    private Long id;

    private String name;

    private Integer isSelf;

    private String shippingType;

    private Integer shippingFee;

    private Integer freeShippingThreshold;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
