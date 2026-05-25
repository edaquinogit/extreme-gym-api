package com.extreme.gym.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaRedirectController {

    // Forward SPA routes to index.html so direct navigation works when backend
    // hosts both API and static assets. Keep API endpoints untouched.
    @GetMapping({
            "/",
            "/dashboard",
            "/dashboard/**",
            "/alunos",
            "/alunos/**",
            "/planos",
            "/planos/**",
            "/matriculas",
            "/matriculas/**",
            "/pagamentos",
            "/pagamentos/**",
            "/checkins",
            "/checkins/**",
            "/acessos",
            "/acessos/**",
            "/catraca",
            "/catraca/**"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}
