package com.yu.user.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("滑块验证请求")
public class SliderCaptchaVerifyDTO implements Serializable {

    @ApiModelProperty(name = "验证码 ID", required = true)
    @NotBlank(message = "captchaId 不能为空")
    private String captchaId;

    @ApiModelProperty(name = "目标横向位移", required = true)
    @NotNull(message = "offsetX 不能为空")
    private Integer offsetX;

    @ApiModelProperty(name = "拖动轨迹", required = true)
    @NotEmpty(message = "dragTrack 不能为空")
    private List<TrackPoint> dragTrack;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackPoint implements Serializable {
        private Integer x;
        private Integer y;
        private Long t;
    }
}
