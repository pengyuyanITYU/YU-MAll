package com.yu.comment.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "CommentDTO", description = "新增评论DTO")
public class CommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品ID", required = true, example = "1001")
    @NotNull(message = "商品ID不能为空")
    private Long itemId;

    @ApiModelProperty(value = "SKU ID", example = "2005")
    private Long skuId;

    @ApiModelProperty(value = "订单ID", required = true, example = "123456789")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "订单明细ID", required = true, example = "556677")
    @NotNull(message = "订单明细ID不能为空")
    private Long orderDetailId;

    @ApiModelProperty(value = "评分(1-5)", required = true, example = "5")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer rating;

    @ApiModelProperty(value = "评论内容", required = true, example = "商品质量很好，发货很快")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容长度不能超过500")
    private String content;

    @ApiModelProperty(value = "图片URL列表")
    @Size(max = 9, message = "图片最多上传9张")
    private List<String> images;

    @ApiModelProperty(value = "是否匿名", example = "false")
    private Boolean isAnonymous;

    @ApiModelProperty(value = "用户昵称")
    private String userNickname;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;
}