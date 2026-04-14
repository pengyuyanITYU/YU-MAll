package com.yu.user.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("滑块挑战响应")
public class SliderCaptchaChallengeVO implements Serializable {

    @ApiModelProperty("验证码 ID")
    private String captchaId;

    @ApiModelProperty("渲染模式")
    private String renderMode;

    @ApiModelProperty("轨道宽度")
    private Integer trackWidth;

    @ApiModelProperty("拖柄宽度")
    private Integer handleWidth;

    @ApiModelProperty("目标区域起点")
    private Integer targetLeft;

    @ApiModelProperty("目标区域宽度")
    private Integer targetWidth;

    @ApiModelProperty("兼容保留字段：背景图片")
    private String backgroundImage;

    @ApiModelProperty("兼容保留字段：滑块图片")
    private String sliderImage;

    @ApiModelProperty("过期时间戳")
    private Long expiresAt;
}
