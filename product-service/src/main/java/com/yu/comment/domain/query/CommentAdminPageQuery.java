package com.yu.comment.domain.query;

import com.yu.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "评论审核分页查询")
public class CommentAdminPageQuery extends PageQuery {

    @ApiModelProperty("商品名称")
    private String itemName;

    @ApiModelProperty("用户昵称")
    private String userNickname;

    @ApiModelProperty("审核状态")
    private Integer status;
}
