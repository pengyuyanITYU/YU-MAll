package com.yu.api.client;

import com.yu.api.config.DefaultFeignConfig;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.domain.query.PageQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        contextId = "internalSearchItemClient",
        name = "yu-mall-product-service",
        path = "/internal/search/items",
        configuration = DefaultFeignConfig.class
)
public interface InternalSearchItemClient {

    @GetMapping("/page")
    TableDataInfo page(@SpringQueryMap PageQuery query);
}
