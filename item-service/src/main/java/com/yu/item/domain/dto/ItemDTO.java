package com.yu.item.domain.dto;

import com.yu.item.domain.po.SpecTemplate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(value = "ItemDTO", description = "商品新增/修改聚合表单")
public class ItemDTO {

    @ApiModelProperty(value = "商品ID (新增时为空，修改时必填)", example = "1")
    private Long id;

    // ==========================================
    // 对应 Item (基础信息)
    // ==========================================

    @ApiModelProperty(value = "商品名称", example = "华为 Mate 60 Pro", required = true)
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @ApiModelProperty(value = "商品副标题/卖点", example = "超可靠玄武架构 | 全焦段超清影像")
    private String subTitle;

    @ApiModelProperty(value = "商品主图/列表图URL", example = "http://img.yu.com/main.jpg", required = true)
    @NotBlank(message = "商品主图不能为空")
    private String image;

    @ApiModelProperty(value = "商品状态：1-上架，2-下架", example = "1")
    private Integer status;

    @ApiModelProperty(value = "商品分类", example = "手机", required = true)
    @NotBlank(message = "商品分类不能为空")
    private String category;

    @ApiModelProperty(value = "商品品牌", example = "华为", required = true)
    @NotBlank(message = "商品品牌不能为空")
    private String brand;

    @ApiModelProperty(value = "商品默认价格（单位：分）", example = "699900", required = true)
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "价格不能小于0")
    private Long price;

    @ApiModelProperty(value = "原价（单位：分）", example = "799900")
    private Integer originalPrice;

    @ApiModelProperty(value = "总库存 (可选，若不传则由后端累加SKU库存)", example = "1000")
    private Integer stock;


    @ApiModelProperty(value = "商品标签，逗号分隔", example = "新品,热销,包邮")
    private String tags;

    // ==========================================
    // 对应 ItemDetail (详情信息)
    // ==========================================

    @ApiModelProperty(value = "详情页富文本内容(HTML)", example = "<div>商品详情...</div>")
    private String detailHtml;

    @ApiModelProperty(value = "商品详情视频URL", example = "http://video.yu.com/v1.mp4")
    private String videoUrl;

    @ApiModelProperty(value = "轮播图列表", required = true)
    @NotEmpty(message = "轮播图不能为空")
    private List<String> bannerImages;

    @ApiModelProperty(value = "规格参数模板(用于前端生成SKU表格列)", example = "[{\"name\":\"颜色\",\"values\":[\"白\",\"黑\"]}]")
    private List<SpecTemplate> specTemplate;

    // ==========================================
    // 对应 ItemSku (SKU列表)
    // ==========================================

    @ApiModelProperty(value = "商品SKU列表", required = true)
    @NotEmpty(message = "至少需要配置一个SKU")
    private List<ItemSkuDTO> skus;

    private Long categoryId;
}