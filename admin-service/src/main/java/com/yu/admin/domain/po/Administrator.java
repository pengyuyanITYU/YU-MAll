package com.yu.admin.domain.po;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.yu.admin.convertor.AdministratorStatusConverter;
import com.yu.admin.enums.AdministratorStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员用户表 sys_user
 */
@Data
@TableName(value="sys_user", autoResultMap = true)
@Accessors(chain = true)
// EasyExcel 样式配置
@HeadRowHeight(20)
@ContentRowHeight(18)
@ColumnWidth(20)
public class Administrator implements Serializable {
    private static final long serialVersionUID = 1L;


    /** 用户ID */
    @TableId(type = IdType.AUTO)
    @ExcelProperty(value = "用户ID", index = 0)
    private Long userId;


    /** 用户账号 */
    @ExcelProperty(value = "用户账号", index = 1)
    private String username;

    /** 用户昵称 */
    @ExcelProperty(value = "用户昵称", index = 2)
    private String nickName;

    /** 密码 (加密存储) */
    @ExcelIgnore // 敏感信息不导出
    private String password;

    /** 用户邮箱 */
    @ExcelProperty(value = "用户邮箱", index = 4)
    @ColumnWidth(25)
    private String email;

    /** 手机号码 */
    @ExcelProperty(value = "手机号码", index = 3)
    private String phone;

    /** 头像地址 */
    @ExcelProperty(value = "头像地址", index = 10)
    @ColumnWidth(50) // URL较长，加宽
    private String avatar;

    /** 帐号状态 (0正常 1停用) */
    @TableField(value = "status")
    @ExcelProperty(value = "帐号状态", index = 5,converter = AdministratorStatusConverter.class)
    private AdministratorStatus status;


    /** 最后登录时间 */
    @ExcelProperty(value = "最后登录时间", index = 6)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(25)
    private LocalDateTime loginDate;

    /** 创建者 */
    @ExcelProperty(value = "创建者", index = 7)
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty(value = "创建时间", index = 8)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(25)
    private LocalDateTime createTime;

    /** 备注 */
    @ExcelProperty(value = "备注", index = 9)
    private String remark;

}