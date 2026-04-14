package com.yu.user.domain.query;

import com.yu.common.domain.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class UserPageQuery extends PageQuery {
    private String levelName;

    private Integer status;
}
