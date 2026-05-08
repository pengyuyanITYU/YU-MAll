package com.yu.search.service;

import com.yu.api.query.SearchItemQuery;
import com.yu.api.vo.SearchItemVO;
import com.yu.common.domain.page.TableDataInfo;

public interface ISearchService {

    TableDataInfo search(SearchItemQuery query);

    void rebuild();

    void upsert(SearchItemVO item);

    void delete(Long itemId);
}
