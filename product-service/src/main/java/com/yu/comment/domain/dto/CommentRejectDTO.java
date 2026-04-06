package com.yu.comment.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "CommentRejectDTO", description = "评论驳回表单")
public class CommentRejectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "驳回原因", required = true)
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 255, message = "驳回原因不能超过255字")
    private String rejectReason;
}
