package com.yu.api.client;

import com.yu.api.config.DefaultFeignConfig;
import com.yu.api.dto.admin.AdminShopDTO;
import com.yu.api.query.admin.AdminShopPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        contextId = "internalAdminShopClient",
        name = "yu-mall-product-service",
        path = "/internal/admin/shops",
        configuration = DefaultFeignConfig.class
)
public interface InternalAdminShopClient {

    @GetMapping("/list")
    TableDataInfo list(@SpringQueryMap AdminShopPageQuery query);

    @GetMapping("/simple")
    AjaxResult<Object> simpleList();

    @GetMapping("/{id}")
    AjaxResult<Object> getById(@PathVariable("id") Long id);

    @PostMapping
    AjaxResult<Void> add(@RequestBody AdminShopDTO shopDTO);

    @PutMapping
    AjaxResult<Void> update(@RequestBody AdminShopDTO shopDTO);

    @DeleteMapping("/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);
}
