package com.yu.api.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminBrandExportVO {
    private Long id;
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
