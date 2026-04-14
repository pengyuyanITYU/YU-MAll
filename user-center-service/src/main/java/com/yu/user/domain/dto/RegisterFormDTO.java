package com.yu.user.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("用户注册表单")
public class RegisterFormDTO implements Serializable {

    @ApiModelProperty(name = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(name = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(name = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @ApiModelProperty(name = "头像")
    private String avatar;

    @ApiModelProperty(name = "昵称", required = true)
    @NotBlank(message = "昵称不能为空")
    private String nickName;

    @ApiModelProperty(name = "滑块票据", required = true)
    private String captchaTicket;
}
