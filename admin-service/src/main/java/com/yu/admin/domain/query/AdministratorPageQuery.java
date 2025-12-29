package com.yu.admin.domain.query;

import com.yu.common.domain.query.PageQuery;
import lombok.Data;

@Data
public class AdministratorPageQuery extends PageQuery {

    private Integer status;

    private String nickName;
}
