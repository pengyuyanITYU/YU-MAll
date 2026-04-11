package com.yu.api.dto.admin;

import lombok.Data;

import java.util.List;

@Data
public class AdminSpecTemplateDTO {

    private String name;

    private List<String> values;
}
