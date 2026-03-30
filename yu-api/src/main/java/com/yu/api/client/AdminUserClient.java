package com.yu.api.client;

import com.yu.api.vo.UserVO;
import com.yu.common.domain.AjaxResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "adminUserClient", name = "yu-mall-user-service", path = "/admin/users")
public interface AdminUserClient {
    @GetMapping("/all")
    AjaxResult<List<UserVO>> getUserInfoByIds(@RequestParam List<Long> ids);
}
