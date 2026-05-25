package com.extreme.gym.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaRedirectController {

    // Safe SPA forward: only forward requests that accept HTML and don't look
    // like asset requests (contain a dot) to avoid intercepting API calls.
    @GetMapping("/**")
    public String forwardSpa(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String path = request.getRequestURI();

        boolean acceptsHtml = accept == null || accept.contains(MediaType.TEXT_HTML_VALUE) || accept.contains("*/*");
        boolean isAsset = path.contains(".");

        if (acceptsHtml && !isAsset) {
            return "forward:/index.html";
        }

        // Let other handlers (API, static resources) process the request
        return "forward:" + path;
    }
}
