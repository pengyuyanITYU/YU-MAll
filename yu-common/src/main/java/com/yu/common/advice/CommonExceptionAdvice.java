package com.yu.common.advice;

import com.yu.common.constant.HttpStatus;
import com.yu.common.domain.AjaxResult;
import com.yu.common.exception.BusinessException;
import com.yu.common.exception.CommonException;
import com.yu.common.exception.DbException;
import com.yu.common.utils.ErrorMessageSanitizer;
import com.yu.common.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.NestedServletException;

import java.net.BindException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {

    @ExceptionHandler(DbException.class)
    public AjaxResult<Void> handleDbException(DbException e) {
        log.error("数据库异常 -> ", e);
        return AjaxResult.error(e.getCode(), ErrorMessageSanitizer.sanitizeDataAccessMessage(e.getMessage()));
    }

    @ExceptionHandler(CommonException.class)
    public AjaxResult<Void> handleCommonException(CommonException e) {
        log.error("自定义异常 -> {}: {}", e.getClass().getSimpleName(), e.getMessage());
        log.debug("", e);
        return AjaxResult.error(e.getCode(), ErrorMessageSanitizer.sanitizeBusinessMessage(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("|"));
        log.error("请求参数校验异常 -> {}", msg);
        log.debug("", e);
        return AjaxResult.error(ErrorMessageSanitizer.sanitizeValidationMessage(msg));
    }

    @ExceptionHandler(BindException.class)
    public AjaxResult<Void> handleBindException(BindException e) {
        log.error("请求参数绑定异常 -> {}", e.getMessage());
        log.debug("", e);
        return AjaxResult.error(ErrorMessageSanitizer.sanitizeValidationMessage(e.getMessage()));
    }

    @ExceptionHandler(NestedServletException.class)
    public AjaxResult<Void> handleNestedServletException(NestedServletException e) {
        log.error("嵌套 Servlet 异常 -> {}", e.getMessage());
        log.debug("", e);
        return AjaxResult.error(ErrorMessageSanitizer.sanitizeBusinessMessage(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public AjaxResult<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return AjaxResult.error(ErrorMessageSanitizer.sanitizeValidationMessage(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<AjaxResult<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("路由不存在 -> {}", e.getMessage());
        return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(AjaxResult.error(HttpStatus.NOT_FOUND, ErrorMessageSanitizer.sanitizeNotFoundMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public AjaxResult<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常 -> {}", e.getMessage());
        log.debug("", e);
        return AjaxResult.error(ErrorMessageSanitizer.sanitizeBusinessMessage(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public AjaxResult<Void> handleRuntimeException(Exception e) {
        String uri = WebUtils.getRequest() == null ? "" : WebUtils.getRequest().getRequestURI();
        log.error("未处理异常 uri: {} -> ", uri, e);
        return AjaxResult.error(ErrorMessageSanitizer.sanitizeSystemMessage());
    }
}
