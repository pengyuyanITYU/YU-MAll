package com.yu.admin.config;

import cn.hutool.core.util.StrUtil;
import com.yu.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!request.getRequestURI().startsWith("/admin")) {
            return true;
        }
        if (StrUtil.isBlank(request.getHeader("user-info"))) {
            throw new UnauthorizedException("unauthorized");
        }
        return true;
    }
}
