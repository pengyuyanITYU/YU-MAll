package com.yu.comment.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@ApiModel(description = "评论详情展示")
@Accessors(chain = true)
public class CommentVO implements Serializable {

    @ApiModelProperty("评论ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("商品ID")
    private Long itemId;

    @ApiModelProperty("SKU ID")
    private Long skuId;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("订单明细ID")
    private Long orderDetailId;

    @ApiModelProperty("用户昵称")
    private String userNickname;

    @ApiModelProperty("用户头像")
    private String userAvatar;

    @ApiModelProperty("评分")
    private Integer rating;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("图片列表")
    private List<String> images;

    @ApiModelProperty("是否匿名")
    private Boolean isAnonymous;

    @ApiModelProperty("审核状态")
    private Integer status;

    @ApiModelProperty("驳回原因")
    private String rejectReason;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("商家回复内容")
    private String merchantReplyContent;

    @ApiModelProperty("商家回复时间")
    private LocalDateTime merchantReplyTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("SKU 规格")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> skuSpecs;

    private String itemName;

    private String itemImage;
}
