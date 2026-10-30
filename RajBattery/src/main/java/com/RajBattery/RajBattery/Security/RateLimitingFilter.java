package com.RajBattery.RajBattery.Security;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    // Per-IP: 30 requests per second
    private static final double PERMITS_PER_SECOND = 30.0;

    // Stricter limit for auth endpoints: 5 req/sec
    private static final double AUTH_PERMITS_PER_SECOND = 5.0;

    private final Map<String, RateLimiter> ipLimiters = new ConcurrentHashMap<>();
    private final Map<String, RateLimiter> authLimiters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String ip = getClientIp(httpReq);
        String path = httpReq.getRequestURI();

        // Skip static assets
        if (path.startsWith("/assestad/") || path.startsWith("/user_assets/") || path.startsWith("/img/")) {
            chain.doFilter(request, response);
            return;
        }

        boolean isAuthPath = path.equals("/login") || path.equals("/user/register/") || path.equals("/signup");

        RateLimiter limiter;
        if (isAuthPath) {
            limiter = authLimiters.computeIfAbsent(ip, k -> RateLimiter.create(AUTH_PERMITS_PER_SECOND));
        } else {
            limiter = ipLimiters.computeIfAbsent(ip, k -> RateLimiter.create(PERMITS_PER_SECOND));
        }

        if (!limiter.tryAcquire()) {
            httpRes.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpRes.setContentType("application/json");
            httpRes.getWriter().write("{\"error\":\"Too many requests. Please slow down.\",\"status\":429}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
