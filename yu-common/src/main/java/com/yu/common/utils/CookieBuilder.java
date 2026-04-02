package com.yu.common.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
@Data
@Accessors(chain = true, fluent = true)
public class CookieBuilder {

    private Charset charset = StandardCharsets.UTF_8;
    private int maxAge = -1;
    private String path = "/";
    private boolean httpOnly;
    private String name;
    private String value;
    private String domain;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public CookieBuilder(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public void build() {
        if (response == null) {
            log.error("response is null, skip add cookie");
            return;
        }
        Cookie cookie = new Cookie(name, URLEncoder.encode(value, charset));
        if (StrUtil.isNotBlank(domain)) {
            cookie.setDomain(domain);
        } else if (request != null) {
            String serverName = request.getServerName();
            serverName = StrUtil.subAfter(serverName, ".", false);
            cookie.setDomain("." + serverName);
        }
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        log.debug("cookie created, charset={}, {}={}, domain={}, maxAge={}, path={}, httpOnly={}",
                charset.name(), name, value, domain, maxAge, path, httpOnly);
        response.addCookie(cookie);
    }

    public String decode(String cookieValue) {
        return URLDecoder.decode(cookieValue, charset);
    }
}