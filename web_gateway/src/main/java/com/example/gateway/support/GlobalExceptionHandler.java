package com.example.gateway.support;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public ApiResponse<Void> handleGateway(GatewayException e, HttpServletRequest request) {
        request.setAttribute(AuditInterceptor.AUDIT_FAILED, true);
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception e, HttpServletRequest request) {
        request.setAttribute(AuditInterceptor.AUDIT_FAILED, true);
        String msg = e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "未知错误" : e.getMessage());
        return ApiResponse.fail(msg);
    }
}
