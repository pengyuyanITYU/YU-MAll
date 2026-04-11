package com.yu.api.client;

import com.yu.api.config.DefaultFeignConfig;
import com.yu.api.dto.admin.AdminCategoryDTO;
import com.yu.api.query.admin.AdminCategoryPageQuery;
import com.yu.api.vo.admin.AdminCategoryExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        contextId = "internalAdminCategoryClient",
        name = "yu-mall-product-service",
        path = "/internal/admin/categories",
        configuration = DefaultFeignConfig.class
)
public interface InternalAdminCategoryClient {

    @PostMapping
    AjaxResult<Void> add(@RequestBody AdminCategoryDTO categoryDTO);

    @DeleteMapping("/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);

    @DeleteMapping("/batch")
    AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids);

    @PutMapping
    AjaxResult<Void> update(@RequestBody AdminCategoryDTO categoryDTO);

    @GetMapping("/{id}")
    AjaxResult<Object> getById(@PathVariable("id") Long id);

    @GetMapping
    TableDataInfo list(@SpringQueryMap AdminCategoryPageQuery query);

    @GetMapping("/simple")
    AjaxResult<Object> simpleList();

    @GetMapping("/export-data")
    AjaxResult<List<AdminCategoryExportVO>> exportData();
}
