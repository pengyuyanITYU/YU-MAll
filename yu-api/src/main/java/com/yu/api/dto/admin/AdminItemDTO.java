package com.yu.api.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminItemDTO {

    private Long id;

    @NotBlank(message = "name must not be blank")
    private String name;

    private String subTitle;

    @NotBlank(message = "image must not be blank")
    private String image;

    private Integer status;

    @NotBlank(message = "category must not be blank")
    private String category;

    @NotBlank(message = "brand must not be blank")
    private String brand;

    @NotNull(message = "price must not be null")
    @Min(value = 0, message = "price must be >= 0")
    private Long price;

    private Integer originalPrice;

    private Integer stock;

    private String tags;

    private String detailHtml;

    private String videoUrl;

    @NotEmpty(message = "bannerImages must not be empty")
    private List<String> bannerImages;

    @Valid
    private List<AdminSpecTemplateDTO> specTemplate;

    @Valid
    @NotEmpty(message = "skus must not be empty")
    private List<AdminItemSkuDTO> skus;

    private Long categoryId;

    @NotNull(message = "shopId must not be null")
    private Long shopId;
}
