package com.yu.search.controller;

import com.yu.api.query.SearchItemQuery;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.search.service.ISearchService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search/items")
@RequiredArgsConstructor
public class SearchController {

    private final ISearchService searchService;

    @GetMapping
    @ApiOperation("搜索商品")
    public TableDataInfo search(SearchItemQuery query) {
        return searchService.search(query);
    }
}
