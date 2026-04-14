package com.yu.api.client;

import com.yu.api.config.DefaultFeignConfig;
import com.yu.api.dto.admin.AdminBrandDTO;
import com.yu.api.query.admin.AdminBrandPageQuery;
import com.yu.api.vo.admin.AdminBrandExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        contextId = "internalAdminBrandClient",
        name = "yu-mall-product-service",
        path = "/internal/admin/brands",
        configuration = DefaultFeignConfig.class
)
public interface InternalAdminBrandClient {

    @PostMapping
    AjaxResult<Void> add(@RequestBody AdminBrandDTO brandDTO);

    @DeleteMapping("/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);

    @DeleteMapping("/batch")
    AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids);

    @PutMapping
    AjaxResult<Void> update(@RequestBody AdminBrandDTO brandDTO);

    @GetMapping("/{id}")
    AjaxResult<Object> getById(@PathVariable("id") Long id);

    @GetMapping
    TableDataInfo list(@SpringQueryMap AdminBrandPageQuery query);

    @GetMapping("/simple")
    AjaxResult<Object> simpleList();

    @GetMapping("/export-data")
    AjaxResult<List<AdminBrandExportVO>> exportData();
}
