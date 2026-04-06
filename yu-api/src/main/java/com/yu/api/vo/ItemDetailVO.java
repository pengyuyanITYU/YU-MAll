package com.yu.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "商品详情")
public class ItemDetailVO {

    @ApiModelProperty(value = "商品id")
    private Long id;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "商品副标题")
    private String subTitle;

    @ApiModelProperty(value = "商品价格")
    private Long price;

    @ApiModelProperty(value = "商品原价")
    private Long originalPrice;

    @ApiModelProperty(value = "商品标签")
    private String tags;

    @ApiModelProperty(value = "商品销量")
    private Integer sold;

    @ApiModelProperty(value = "评论数")
    private Integer commentCount;

    @ApiModelProperty(value = "好评率")
    private Integer positiveRate;

    @ApiModelProperty(value = "商品评分")
    private BigDecimal avgScore;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "分类")
    private String category;

    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    @ApiModelProperty(value = "品牌")
    private String brand;

    @ApiModelProperty(value = "店铺ID")
    private Long shopId;

    @ApiModelProperty(value = "店铺名称")
    private String shopName;

    @ApiModelProperty(value = "是否自营")
    private Integer isSelf;

    @ApiModelProperty(value = "运费模式")
    private String shippingType;

    @ApiModelProperty(value = "固定运费")
    private Integer shippingFee;

    @ApiModelProperty(value = "满额包邮门槛")
    private Integer freeShippingThreshold;

    @ApiModelProperty(value = "运费说明")
    private String shippingDesc;

    @ApiModelProperty(value = "商品轮播图")
    private List<String> bannerImages;

    @ApiModelProperty(value = "商品视频")
    private String videoUrl;

    @ApiModelProperty(value = "商品详情html")
    private String detailHtml;

    @ApiModelProperty(value = "商品规格选择器模板")
    private List<SpecTemplateItem> specTemplate;

    @ApiModelProperty(value = "商品SKU列表")
    private List<SkuVO> skuList;

    @Data
    public static class SpecTemplateItem {
        @ApiModelProperty(value = "规格模板项名称")
        private String name;

        @ApiModelProperty(value = "规格模板项值列表")
        private List<String> values;
    }

    @Data
    public static class SkuVO {
        @ApiModelProperty(value = "商品SKU id")
        private Long id;

        @ApiModelProperty(value = "商品SKU规格")
        private Map<String, String> specs;

        @ApiModelProperty(value = "商品SKU价格")
        private Long price;

        @ApiModelProperty(value = "商品SKU库存")
        private Integer stock;

        @ApiModelProperty(value = "商品SKU图片")
        private String image;
    }
}
