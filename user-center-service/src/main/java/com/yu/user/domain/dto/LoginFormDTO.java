package com.yu.user.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户登录表单")
public class LoginFormDTO implements Serializable {

    @ApiModelProperty(name = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(name = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(name = "是否记住登录")
    private Boolean rememberMe = false;

    @ApiModelProperty(name = "滑块票据")
    private String captchaTicket;
}
