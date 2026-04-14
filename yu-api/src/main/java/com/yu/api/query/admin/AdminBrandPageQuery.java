package com.yu.api.query.admin;

import com.yu.common.domain.query.PageQuery;
import lombok.Data;

@Data
public class AdminBrandPageQuery extends PageQuery {
    private String name;
}
