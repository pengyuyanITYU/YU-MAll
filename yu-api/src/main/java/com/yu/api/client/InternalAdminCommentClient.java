package com.yu.api.client;

import com.yu.api.config.DefaultFeignConfig;
import com.yu.api.dto.admin.AdminCommentRejectDTO;
import com.yu.api.query.admin.AdminCommentPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        contextId = "internalAdminCommentClient",
        name = "yu-mall-product-service",
        path = "/internal/admin/comments",
        configuration = DefaultFeignConfig.class
)
public interface InternalAdminCommentClient {

    @GetMapping("/list")
    TableDataInfo list(@SpringQueryMap AdminCommentPageQuery query);

    @GetMapping("/{id}")
    AjaxResult<Object> getById(@PathVariable("id") Long id);

    @PutMapping("/{id}/approve")
    AjaxResult<Void> approve(@PathVariable("id") Long id);

    @PutMapping("/{id}/reject")
    AjaxResult<Void> reject(@PathVariable("id") Long id, @RequestBody AdminCommentRejectDTO rejectDTO);
}
