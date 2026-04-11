package com.yu.api.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminItemExportVO {

    private Long id;

    private String name;

    private String subTitle;

    private String image;

    private Long price;

    private Integer originalPrice;

    private Integer stock;

    private Integer sold;

    private String tags;

    private Integer status;

    private String category;

    private String brand;

    private Long categoryId;

    private Long shopId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
