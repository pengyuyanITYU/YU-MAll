package com.yu.api.query.admin;

import com.yu.common.domain.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminOrderPageQuery extends PageQuery {

    private Integer status;

    private Integer paymentType;

    private Long userId;
}
