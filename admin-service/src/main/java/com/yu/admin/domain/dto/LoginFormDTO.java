package com.yu.admin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(description = "登录参数")
public class LoginFormDTO implements Serializable {
    @NotBlank(message = "账号不能为空")
    @ApiModelProperty(value = "用户账号", required = true, example = "admin", position = 1)
    private String username;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "用户密码", required = true, example = "123456", position = 2)
    private String password;

//    @ApiModelProperty(value = "验证码", example = "8888", position = 3)
//    private String code;

//    @ApiModelProperty(value = "验证码唯一标识(UUID)", example = "a1b2c3d4-...", position = 4)
//    private String uuid;
}
