package com.yu.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@ApiModel(description = "user vo")
@Accessors(chain = true)
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "user id")
    private Long id;

    @ApiModelProperty(name = "username")
    private String username;

    @ApiModelProperty(name = "nickname")
    private String nickName;

    @ApiModelProperty(name = "avatar")
    private String avatar;

    @ApiModelProperty(name = "balance")
    private Long balance;

    @ApiModelProperty(name = "level name")
    private String levelName;

    @ApiModelProperty(name = "current points")
    private Integer currentPoints;

    private String phone;

    private String email;

    private Integer gender;

    @ApiModelProperty("birthday")
    @JsonFormat(pattern = "yyyy/MM/dd")
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate birthday;
}
