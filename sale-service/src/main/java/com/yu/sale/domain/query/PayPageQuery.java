package com.yu.sale.domain.query;


import com.yu.common.domain.query.PageQuery;
import lombok.Data;

@Data
public class PayPageQuery extends PageQuery {

    private String payType;

    private Integer status;
}
