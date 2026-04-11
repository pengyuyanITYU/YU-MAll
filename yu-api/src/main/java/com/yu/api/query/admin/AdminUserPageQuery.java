package com.yu.api.query.admin;

import com.yu.common.domain.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserPageQuery extends PageQuery {

    private String levelName;

    private Integer status;
}
