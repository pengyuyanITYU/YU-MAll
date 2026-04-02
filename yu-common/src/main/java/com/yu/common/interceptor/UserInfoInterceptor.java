package com.yu.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.yu.common.utils.AdministratorContext;
import com.yu.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("user-info");
        if (StrUtil.isNotBlank(header)) {
            Long userId = Long.valueOf(header);
            UserContext.setUser(userId);
            AdministratorContext.setUser(userId);
            System.out.println("[Interceptor] Thread: " + Thread.currentThread().getName() +
                    " | User: " + UserContext.getUser());
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
        AdministratorContext.removeUser();
    }
}

