package com.yu.item.domain.po;

import lombok.Data;

import java.util.List;

@Data
public class SpecTemplate {
    private String name;
    private List<String> values;
}
