package com.employees.employees_be.middleware;

import org.springframework.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String SECRET_HEADER = "secret";
    private static final String SECRET_VALUE = "fally";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                             @NonNull HttpServletResponse response, 
                             @NonNull Object handler) throws Exception {
        
        String headerValue = request.getHeader(SECRET_HEADER);

        if (headerValue == null || !headerValue.equals(SECRET_VALUE)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized: Invalid or missing secret header");
            return false;
        }

        return true;
    }
}
