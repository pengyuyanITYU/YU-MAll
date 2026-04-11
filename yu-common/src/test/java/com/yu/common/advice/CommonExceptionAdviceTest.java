package com.yu.common.advice;

import com.yu.common.domain.AjaxResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonExceptionAdviceTest {

    @Test
    void shouldReturn404ResponseForMissingResource() throws Exception {
        CommonExceptionAdvice advice = new CommonExceptionAdvice();
        Method handler = Arrays.stream(CommonExceptionAdvice.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("handleNoResourceFoundException"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing NoResourceFoundException handler"));

        Object result = handler.invoke(advice, new NoResourceFoundException(HttpMethod.GET, "/admin/items/list"));

        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        AjaxResult<?> body = (AjaxResult<?>) response.getBody();
        assertEquals(404, body.getCode());
        assertEquals("No static resource /admin/items/list.", body.getMsg());
    }
}
