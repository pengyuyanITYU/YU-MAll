package com.yu.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "AiChatResponseVO", description = "AI对话响应")
public class AiChatResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("AI回复文本")
    private String reply;

    @ApiModelProperty("实际使用模型")
    private String model;
}
