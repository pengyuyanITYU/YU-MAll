package com.yu.item.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemDetailVO {

    private Long id;

    private String name;

    private String subTitle;

    private String image;

    private Long price;

    private Integer originalPrice;

    private Integer stock;

    private String tags;

    private Integer sold;

    private BigDecimal avgScore;

    private Integer status;

    private String category;

    private Long categoryId;

    private String brand;

    private List<String> bannerImages;

    private String videoUrl;

    private String detailHtml;

    private List<SpecTemplateItem> specTemplate;

    private List<SkuVO> skuList;

    @Data
    public static class SpecTemplateItem {
        private String name;
        private List<String> values;
    }

    @Data
    public static class SkuVO {
        private Long id;
        private Map<String, String> specs;
        private Long price;
        private Integer stock;
        private String image;
    }
}
