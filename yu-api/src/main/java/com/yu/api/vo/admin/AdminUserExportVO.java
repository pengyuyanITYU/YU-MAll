package com.yu.api.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserExportVO {

    private Long id;

    private String username;

    private String phone;

    private Integer status;

    private Long balance;

    private String avatar;

    private String nickName;

    private String levelName;

    private Integer currentPoints;

    private String email;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
