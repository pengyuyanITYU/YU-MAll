package com.yu.api.dto;

import com.yu.api.enums.SearchSyncAction;
import com.yu.api.vo.SearchItemVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class SearchItemIndexEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private SearchSyncAction action;

    private Long itemId;

    private SearchItemVO item;
}
