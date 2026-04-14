package com.yu.item.domain.query;

import com.yu.common.domain.query.PageQuery;
import lombok.Data;

@Data
public class BrandQuery extends PageQuery {
    private String name;
}
