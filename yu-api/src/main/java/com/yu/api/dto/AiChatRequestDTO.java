package com.yu.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "AiChatRequestDTO", description = "AI对话请求")
public class AiChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户消息", required = true)
    @NotBlank(message = "消息不能为空")
    private String message;

    @ApiModelProperty(value = "模型名称，可选")
    private String model;

    @ApiModelProperty(value = "采样温度，可选")
    private Double temperature;
}
