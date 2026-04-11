package com.yu.api.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCategoryDTO {

    private Long id;

    @NotBlank(message = "category name must not be blank")
    private String name;
}
