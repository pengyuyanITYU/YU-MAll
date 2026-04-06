package com.yu.item.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel(value = "ShopDTO", description = "店铺新增/修改参数")
public class ShopDTO {

    @ApiModelProperty("店铺ID")
    private Long id;

    @NotBlank(message = "店铺名称不能为空")
    @ApiModelProperty(value = "店铺名称", required = true)
    private String name;

    @NotNull(message = "是否自营不能为空")
    @ApiModelProperty(value = "是否自营", required = true)
    private Integer isSelf;

    @NotBlank(message = "运费模式不能为空")
    @ApiModelProperty(value = "运费模式", required = true)
    private String shippingType;

    @NotNull(message = "固定运费不能为空")
    @Min(value = 0, message = "固定运费不能小于0")
    @ApiModelProperty(value = "固定运费（分）", required = true)
    private Integer shippingFee;

    @Min(value = 0, message = "满额包邮门槛不能小于0")
    @ApiModelProperty("满额包邮门槛（分）")
    private Integer freeShippingThreshold;

    @NotNull(message = "状态不能为空")
    @ApiModelProperty(value = "状态", required = true)
    private Integer status;
}
