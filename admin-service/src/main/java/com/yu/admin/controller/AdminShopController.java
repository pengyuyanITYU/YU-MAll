package com.yu.admin.controller;

import com.yu.admin.service.AdminShopService;
import com.yu.api.dto.admin.AdminShopDTO;
import com.yu.api.query.admin.AdminShopPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/shops")
public class AdminShopController {

    private final AdminShopService adminShopService;

    @GetMapping("/list")
    public TableDataInfo list(AdminShopPageQuery query) {
        return adminShopService.list(query);
    }

    @GetMapping("/simple")
    public AjaxResult<Object> simpleList() {
        return adminShopService.simpleList();
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return adminShopService.getById(id);
    }

    @PostMapping
    public AjaxResult<Void> add(@Valid @RequestBody AdminShopDTO shopDTO) {
        return adminShopService.add(shopDTO);
    }

    @PutMapping
    public AjaxResult<Void> update(@Valid @RequestBody AdminShopDTO shopDTO) {
        return adminShopService.update(shopDTO);
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return adminShopService.delete(id);
    }
}
