package com.yu.user.domain.po;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yu.user.converter.UserStatusConverter;
import com.yu.user.enums.UserStatus;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
// EasyExcel 样式配置
@HeadRowHeight(20)
@ContentRowHeight(18)
@ColumnWidth(20)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty(value = "用户ID", index = 0)
    private Long id;

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    @ExcelProperty(value = "用户名", index = 1)
    private String username;

    /**
     * 密码，加密存储
     */
    @ApiModelProperty("密码，加密存储")
    @ExcelIgnore // 敏感字段不导出
    private String password;

    /**
     * 注册手机号
     */
    @ApiModelProperty("注册手机号")
    @ExcelProperty(value = "手机号", index = 2)
    @ColumnWidth(20)
    private String phone;

    /**
     * 创建时间
     */

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "注册时间", index = 11)
    @ColumnWidth(25)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ExcelProperty(value = "更新时间", index = 12)
    @ColumnWidth(25)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 使用状态（1正常 2冻结）
     */
    @ApiModelProperty("使用状态")
    @ExcelProperty(value = "状态", index = 5, converter = UserStatusConverter.class)
    private UserStatus status;

    /**
     * 账户余额
     */
    @ApiModelProperty("账户余额")
    @ExcelProperty(value = "余额", index = 6)
    private Long balance;

    /**
     * 用户头像
     * */
    @ApiModelProperty("用户头像")
    @ExcelProperty(value = "头像", index = 13)
    @ColumnWidth(50)
    private String avatar;

    /**
     * 用户昵称
     * */
    @ApiModelProperty("用户昵称")
    @ExcelProperty(value = "昵称", index = 3)
    private String nickName;

    /**
     * 会员等级ID
     * */
    @ApiModelProperty("会员等级ID")
    @ExcelProperty(value = "会员等级", index = 4)
    private String levelName;

    /**
     * 支付密码
     * */
    @ApiModelProperty("支付密码")
    @ExcelIgnore // 敏感字段不导出
    private String payPassword;

    /*
     * 当前会员积分
     * */
    @ApiModelProperty("当前会员积分")
    @ExcelProperty(value = "积分", index = 7)
    private Integer currentPoints;

    @ApiModelProperty("会员等级ID")
    @ExcelIgnore // 导出等级名称即可，ID忽略
    private Integer memberLevelId;

    @ExcelProperty(value = "邮箱", index = 8)
    private String email;

    @ExcelProperty(value = "性别", index = 9)
    private Integer gender;

    @ApiModelProperty("生日")
    @JsonFormat(pattern = "yyyy/MM/dd")
    @ExcelProperty(value = "生日", index = 10)
    @DateTimeFormat("yyyy年MM月dd日")
    private LocalDate birthday;

}