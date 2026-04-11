package com.yu.api.client;

import com.yu.api.config.DefaultFeignConfig;
import com.yu.api.query.admin.AdminOrderPageQuery;
import com.yu.api.vo.admin.AdminOrderExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        contextId = "internalAdminOrderClient",
        name = "yu-mall-order-service",
        path = "/internal/admin/orders",
        configuration = DefaultFeignConfig.class
)
public interface InternalAdminOrderClient {

    @GetMapping("/list")
    TableDataInfo list(@SpringQueryMap AdminOrderPageQuery query);

    @GetMapping("/{id}")
    AjaxResult<Object> getById(@PathVariable("id") Long id);

    @DeleteMapping("/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);

    @DeleteMapping
    AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/recent")
    AjaxResult<Object> recent();

    @GetMapping("/export-data")
    AjaxResult<List<AdminOrderExportVO>> exportData();
}
