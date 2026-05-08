package com.yu.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SearchItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String subTitle;

    private String image;

    private Long price;

    private Long originalPrice;

    private String brand;

    private String category;

    private Long shopId;

    private String shopName;

    private Integer isSelf;

    private String shippingType;

    private Integer shippingFee;

    private Integer freeShippingThreshold;

    private String shippingDesc;

    private Integer sold;

    private Integer commentCount;

    private Integer positiveRate;

    private BigDecimal avgScore;

    private Integer status;

    private LocalDateTime updateTime;
}
