package com.yu.user.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("滑块挑战请求")
public class SliderCaptchaChallengeDTO implements Serializable {

    @ApiModelProperty(name = "场景", required = true, example = "LOGIN")
    @NotBlank(message = "scene 不能为空")
    private String scene;
}
