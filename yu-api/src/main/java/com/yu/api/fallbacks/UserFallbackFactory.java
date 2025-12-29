package com.yu.api.fallbacks;


import com.yu.api.client.UserClient;
import com.yu.api.vo.UserVO;
import com.yu.common.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {

            @Override
            public AjaxResult<List<UserVO>> getUserInfoByIds(List<Long> ids) {
                return null;
            }
        };
    }
}
