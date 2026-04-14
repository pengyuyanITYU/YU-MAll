package com.yu.api.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminBrandDTO {

    private Long id;

    @NotBlank(message = "brand name must not be blank")
    private String name;
}
