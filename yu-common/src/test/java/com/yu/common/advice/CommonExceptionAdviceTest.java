package com.yu.common.advice;

import com.yu.common.domain.AjaxResult;
import com.yu.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonExceptionAdviceTest {

    @Test
    void shouldReturnSafeMessageForMissingResource() {
        CommonExceptionAdvice advice = new CommonExceptionAdvice();
        ResponseEntity<?> response = advice.handleNoResourceFoundException(
                new NoResourceFoundException(HttpMethod.GET, "/admin/items/list")
        );
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        AjaxResult<?> body = (AjaxResult<?>) response.getBody();
        assertEquals(404, body.getCode());
        assertEquals("请求地址不存在", body.getMsg());
    }

    @Test
    void shouldKeepBusinessMessageForBusinessException() {
        CommonExceptionAdvice advice = new CommonExceptionAdvice();

        AjaxResult<Void> result = advice.handleBusinessException(new BusinessException("删除商品前请先下架"));

        assertEquals(500, result.getCode());
        assertEquals("删除商品前请先下架", result.getMsg());
    }

    @Test
    void shouldHideTechnicalMessageForUnhandledException() {
        CommonExceptionAdvice advice = new CommonExceptionAdvice();

        AjaxResult<Void> result = advice.handleRuntimeException(
                new RuntimeException("org.mybatis.spring.MyBatisSystemException: nested exception is java.sql.SQLSyntaxErrorException")
        );

        assertEquals(500, result.getCode());
        assertEquals("系统异常，请稍后重试", result.getMsg());
    }
}
