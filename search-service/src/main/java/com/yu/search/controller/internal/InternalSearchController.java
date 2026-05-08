package com.yu.search.controller.internal;

import com.yu.common.domain.AjaxResult;
import com.yu.search.service.ISearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/search/items")
@RequiredArgsConstructor
public class InternalSearchController {

    private final ISearchService searchService;

    @PostMapping("/rebuild")
    public AjaxResult<Void> rebuild() {
        searchService.rebuild();
        return AjaxResult.success();
    }
}
