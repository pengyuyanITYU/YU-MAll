package com.yu.member.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@ApiModel(description = "会员等级保存/更新DTO")
@Data
public class MemberDTO {

    @NotBlank(message = "等级名称不能为空")
    @Size(max = 50, message = "等级名称长度不能超过50")
    @ApiModelProperty("等级名称")
    private String levelName;

    @NotNull(message = "最低积分不能为空")
    @Min(value = 0, message = "最低积分不能小于0")
    @ApiModelProperty("最低积分")
    private Integer minPoints;

    @NotNull(message = "最高积分不能为空")
    @ApiModelProperty("最高积分，-1 代表不封顶")
    private Integer maxPoints;

    @NotNull(message = "折扣率不能为空")
    @DecimalMin(value = "0.00", message = "折扣率不能小于0")
    @DecimalMax(value = "1.00", message = "折扣率不能大于1")
    @Digits(integer = 1, fraction = 2, message = "折扣率格式错误，例如0.95")
    @ApiModelProperty("折扣率，例如0.95表示95折")
    private BigDecimal discountRate;
}