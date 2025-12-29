package com.yu.api.client;

import com.yu.api.fallbacks.UserFallbackFactory;
import com.yu.api.vo.UserVO;
import com.yu.common.domain.AjaxResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name="yu-mall-user-service",path="/admin/users",fallbackFactory = UserFallbackFactory.class)
public interface UserClient {



    @GetMapping("/all")
    public AjaxResult<List<UserVO>> getUserInfoByIds(@RequestParam List<Long> ids);
}
