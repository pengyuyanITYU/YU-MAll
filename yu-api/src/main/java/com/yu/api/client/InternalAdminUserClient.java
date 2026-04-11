package com.yu.api.client;

import com.yu.api.config.DefaultFeignConfig;
import com.yu.api.query.admin.AdminUserPageQuery;
import com.yu.api.vo.UserVO;
import com.yu.api.vo.admin.AdminUserExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        contextId = "internalAdminUserClient",
        name = "yu-mall-user-center-service",
        path = "/internal/admin/users",
        configuration = DefaultFeignConfig.class
)
public interface InternalAdminUserClient {

    @GetMapping
    TableDataInfo list(@SpringQueryMap AdminUserPageQuery query);

    @GetMapping("/{id}")
    AjaxResult<Object> getById(@PathVariable("id") Long id);

    @GetMapping("/all")
    AjaxResult<List<UserVO>> getByIds(@RequestParam("ids") List<Long> ids);

    @PutMapping("/{id}/{status}")
    AjaxResult<Void> updateStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status);

    @GetMapping("/export-data")
    AjaxResult<List<AdminUserExportVO>> exportData();
}
