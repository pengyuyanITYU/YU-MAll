package com.yu.item.domain.dto;

import com.yu.item.domain.po.SpecTemplate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "ItemDTO", description = "商品新增/修改参数")
public class ItemDTO {

    @ApiModelProperty(value = "商品ID")
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @ApiModelProperty(value = "商品名称", required = true)
    private String name;

    @ApiModelProperty(value = "商品副标题")
    private String subTitle;

    @NotBlank(message = "商品主图不能为空")
    @ApiModelProperty(value = "商品主图", required = true)
    private String image;

    @ApiModelProperty(value = "商品状态：1上架，2下架")
    private Integer status;

    @NotBlank(message = "商品分类不能为空")
    @ApiModelProperty(value = "商品分类", required = true)
    private String category;

    @NotBlank(message = "商品品牌不能为空")
    @ApiModelProperty(value = "商品品牌", required = true)
    private String brand;

    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格不能小于0")
    @ApiModelProperty(value = "商品价格(分)", required = true)
    private Long price;

    @ApiModelProperty(value = "原价(分)")
    private Integer originalPrice;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "商品标签")
    private String tags;

    @ApiModelProperty(value = "商品详情HTML")
    private String detailHtml;

    @ApiModelProperty(value = "商品视频URL")
    private String videoUrl;

    @NotEmpty(message = "轮播图不能为空")
    @ApiModelProperty(value = "轮播图", required = true)
    private List<String> bannerImages;

    @ApiModelProperty(value = "规格模板")
    private List<SpecTemplate> specTemplate;

    @NotEmpty(message = "SKU不能为空")
    @ApiModelProperty(value = "SKU列表", required = true)
    private List<ItemSkuDTO> skus;

    @ApiModelProperty(value = "分类ID")
    private Long categoryId;
}