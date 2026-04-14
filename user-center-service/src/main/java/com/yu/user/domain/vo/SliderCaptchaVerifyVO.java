package com.yu.user.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("滑块验证结果")
public class SliderCaptchaVerifyVO implements Serializable {

    @ApiModelProperty("验证票据")
    private String captchaTicket;

    @ApiModelProperty("过期时间戳")
    private Long expiresAt;
}
