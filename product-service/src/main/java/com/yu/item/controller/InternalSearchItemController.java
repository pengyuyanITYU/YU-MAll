package com.yu.item.controller;

import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.domain.query.PageQuery;
import com.yu.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/search/items")
public class InternalSearchItemController {

    private final IItemService itemService;

    @GetMapping("/page")
    public TableDataInfo page(PageQuery query) {
        return itemService.listSearchItems(query);
    }
}
