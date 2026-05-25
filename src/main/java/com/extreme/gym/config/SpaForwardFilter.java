package com.extreme.gym.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaForwardFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accept = request.getHeader("Accept");
        String path = request.getRequestURI();

        boolean acceptsHtml = accept != null && accept.contains("text/html");
        boolean isGet = "GET".equalsIgnoreCase(request.getMethod());
        boolean hasExtension = path.contains(".");

        if (isGet && acceptsHtml && !hasExtension && !path.startsWith("/auth") && !path.startsWith("/v3") && !path.startsWith("/swagger") && !path.startsWith("/h2-console")) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
