package com.yu.api.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminShopDTO {

    private Long id;

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotNull(message = "isSelf must not be null")
    private Integer isSelf;

    @NotBlank(message = "shippingType must not be blank")
    private String shippingType;

    @NotNull(message = "shippingFee must not be null")
    @Min(value = 0, message = "shippingFee must be >= 0")
    private Integer shippingFee;

    @Min(value = 0, message = "freeShippingThreshold must be >= 0")
    private Integer freeShippingThreshold;

    @NotNull(message = "status must not be null")
    private Integer status;
}
