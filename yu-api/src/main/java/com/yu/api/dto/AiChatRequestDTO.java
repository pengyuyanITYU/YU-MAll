package com.yu.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.Valid;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "AiChatRequestDTO", description = "AI chat request")
public class AiChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "User message. Optional when attachments are provided")
    private String message;

    @ApiModelProperty(value = "Model name")
    private String model;

    @ApiModelProperty(value = "Sampling temperature")
    private Double temperature;

    @Valid
    @ApiModelProperty(value = "Attachments. Supports image/pdf/word/excel")
    private List<AiChatAttachmentDTO> attachments;
}