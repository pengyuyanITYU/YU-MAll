package com.yu.api.client;

import com.yu.api.config.DefaultFeignConfig;
import com.yu.api.dto.admin.AdminItemDTO;
import com.yu.api.query.admin.AdminItemPageQuery;
import com.yu.api.vo.admin.AdminItemExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        contextId = "internalAdminItemClient",
        name = "yu-mall-product-service",
        path = "/internal/admin/items",
        configuration = DefaultFeignConfig.class
)
public interface InternalAdminItemClient {

    @GetMapping("/list")
    TableDataInfo list(@SpringQueryMap AdminItemPageQuery query);

    @GetMapping("/{id}")
    AjaxResult<Object> getById(@PathVariable("id") Long id);

    @GetMapping("/batch")
    AjaxResult<Object> getByIds(@RequestParam("ids") List<Long> ids);

    @DeleteMapping("/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);

    @DeleteMapping
    AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids);

    @PostMapping
    AjaxResult<Void> add(@RequestBody AdminItemDTO itemDTO);

    @PutMapping
    AjaxResult<Void> update(@RequestBody AdminItemDTO itemDTO);

    @GetMapping("/dashboard")
    AjaxResult<Object> dashboard();

    @GetMapping("/export-data")
    AjaxResult<List<AdminItemExportVO>> exportData();
}
