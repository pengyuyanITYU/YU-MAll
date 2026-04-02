package com.yu.user.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@ApiModel("用户基础信息")
public class UserBasicInfoDTO {

    @ApiModelProperty("昵称")
    @NotNull(message = "昵称不能为空")
    private String nickName;

    @ApiModelProperty("头像")
    @NotNull(message = "头像不能为空")
    private String avatar;

    @ApiModelProperty("手机号")
    @NotNull(message = "手机号不能为空")
    private String phone;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("性别")
    @Max(value = 1, message = "性别仅支持 0 或 1")
    private Integer gender;

    @ApiModelProperty("生日")
    @JsonFormat(pattern = "yyyy/MM/dd")
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate birthday;
}