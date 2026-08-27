package com.example.gateway.support;

import com.example.gateway.service.AdminAuditService;
import com.example.gateway.session.SessionManager;
import com.example.gateway.session.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class AuditInterceptor implements HandlerInterceptor {
    public static final String AUDIT_FAILED = AuditInterceptor.class.getName() + ".failed";
    private static final String START = AuditInterceptor.class.getName() + ".start";
    private static final String TRACE = AuditInterceptor.class.getName() + ".trace";
    private final AdminAuditService audit;
    private final SessionManager sessions;

    public AuditInterceptor(AdminAuditService audit, SessionManager sessions) {
        this.audit = audit;
        this.sessions = sessions;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START, System.currentTimeMillis());
        String traceId = UUID.randomUUID().toString().replace("-", "");
        request.setAttribute(TRACE, traceId);
        response.setHeader("X-Trace-Id", traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long start = request.getAttribute(START) instanceof Long ? (Long) request.getAttribute(START) : System.currentTimeMillis();
        String traceId = String.valueOf(request.getAttribute(TRACE));
        UserSession actor = sessions.find(request.getHeader("X-Token"));
        audit.record(traceId, actor, action(request), request.getMethod(), request.getRequestURI(),
                null, null, request.getQueryString(), ex == null && response.getStatus() < 400
                        && !Boolean.TRUE.equals(request.getAttribute(AUDIT_FAILED)),
                System.currentTimeMillis() - start, request.getRemoteAddr());
    }

    private static String action(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.endsWith("/login")) return "LOGIN";
        if (path.endsWith("/logout")) return "LOGOUT";
        if (path.contains("/upload")) return "STREAM_UPLOAD";
        if (path.contains("/query")) return "QUERY";
        if ("DELETE".equals(request.getMethod())) return "DELETE";
        if ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod())) return "CHANGE";
        return "VIEW";
    }
}
