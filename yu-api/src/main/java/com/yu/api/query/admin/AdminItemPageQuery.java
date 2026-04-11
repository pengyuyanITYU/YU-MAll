package com.yu.api.query.admin;

import com.yu.common.domain.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminItemPageQuery extends PageQuery {

    private String name;

    private String category;

    private String brand;

    private String sold;

    private Integer minPrice;

    private Integer maxPrice;
}
