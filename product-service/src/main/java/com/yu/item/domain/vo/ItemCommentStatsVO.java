package com.yu.item.domain.vo;

import lombok.Data;

@Data
public class ItemCommentStatsVO {

    private Long itemId;

    private Integer approvedCount;

    private Integer positiveCount;
}
