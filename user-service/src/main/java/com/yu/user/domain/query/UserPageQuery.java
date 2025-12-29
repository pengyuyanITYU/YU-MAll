package com.yu.user.domain.query;

import com.yu.common.domain.query.PageQuery;
import lombok.Data;

@Data
public class UserPageQuery extends PageQuery {

    private String levelName;

    private Integer status;
}
