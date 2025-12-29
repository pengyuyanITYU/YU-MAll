package com.yu.admin.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yu.admin.enums.AdministratorStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@ApiModel
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AdministratorVO {

    @ApiModelProperty(value = "用户ID", example = "101")
    private Long userId;

    @ApiModelProperty(value = "用户账号", example = "admin")
    private String username;

    @ApiModelProperty(value = "用户状态", example = "1")
    private AdministratorStatus status;

    @ApiModelProperty(value = "用户昵称", example = "超级管理员")
    private String nickName;

    @ApiModelProperty(value = "头像地址", example = "http://mall.com/avatar/1.jpg")
    private String avatar;

    @ApiModelProperty(value = "手机号码", example = "15888888888")
    private String phone;

    @ApiModelProperty(value = "邮箱", example = "admin@mall.com")
    private String email;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String token;

//
//    @ApiModelProperty(value = "所属角色标识集合", notes = "用于前端判断角色", example = "[\"admin\", \"common\"]")
//    private Set<String> roles;
//
//    @ApiModelProperty(value = "所属权限标识集合", notes = "用于前端控制按钮显示", example = "[\"system:user:add\", \"system:user:edit\"]")
//    private Set<String> permissions;
}
