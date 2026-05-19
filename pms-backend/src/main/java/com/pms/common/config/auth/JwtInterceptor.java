package com.pms.common.config.auth;

import com.pms.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.contains("/auth/login") || path.contains("/doc.html") || path.contains("/swagger") || path.contains("/v3/api-docs")) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权\"}");
            return false;
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            Claims claims = JwtUtils.parse(token);
            request.setAttribute("userId", Long.valueOf(claims.getId()));
            request.setAttribute("username", claims.getSubject());
        } catch (ExpiredJwtException e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":1002,\"message\":\"令牌已过期\"}");
            return false;
        } catch (SignatureException e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":1003,\"message\":\"无效令牌\"}");
            return false;
        }
        return true;
    }
}
