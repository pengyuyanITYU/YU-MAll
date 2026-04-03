package com.yu.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@ApiModel(value = "AiChatAttachmentDTO", description = "AI chat attachment")
public class AiChatAttachmentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Attachment url cannot be blank")
    @ApiModelProperty(value = "Attachment url", required = true)
    private String url;

    @ApiModelProperty(value = "Attachment file name")
    private String fileName;

    @ApiModelProperty(value = "Attachment MIME type")
    private String contentType;
}