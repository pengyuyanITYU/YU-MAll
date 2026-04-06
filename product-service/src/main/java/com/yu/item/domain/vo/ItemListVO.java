package com.yu.item.domain.vo;

import lombok.Data;

@Data
public class ItemListVO {

    private Long id;

    private String name;

    private String subTitle;

    private String image;

    private Long price;

    private Integer originalPrice;

    private Integer sold;

    private Integer commentCount;

    private Integer positiveRate;

    private String brand;

    private String category;

    private Long shopId;

    private String shopName;

    private Integer isSelf;

    private String shippingType;

    private Integer shippingFee;

    private Integer freeShippingThreshold;

    private String shippingDesc;
}
