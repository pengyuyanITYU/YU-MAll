package com.yu.api.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class AdminItemSkuDTO {

    private Long id;

    @NotNull(message = "specs must not be null")
    private Map<String, String> specs;

    @NotNull(message = "price must not be null")
    @Min(value = 0, message = "price must be >= 0")
    private Long price;

    @NotNull(message = "stock must not be null")
    @Min(value = 0, message = "stock must be >= 0")
    private Integer stock;

    private String image;
}
