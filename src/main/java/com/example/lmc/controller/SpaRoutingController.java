package com.example.lmc.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SpaRoutingController {

    // Serve a raiz normalmente
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    // Catch-all para rotas do SPA (compatível com PathPatternParser)
    @GetMapping("/{*path}")
    public String forwardSpaRequests(@PathVariable String path) {

        // 1) NÃO interceptar recursos estáticos (arquivos com ponto: .js, .css, .png etc.)
        if (path.contains(".")) {
            return "forward:/" + path;
        }

        // 2) NÃO interceptar endpoints do backend / docs
        if (path.startsWith("api/") || path.startsWith("v3/") || path.startsWith("swagger-ui/")) {
            return "forward:/" + path;
        }

        // 3) Qualquer outra rota -> SPA (index.html)
        return "forward:/index.html";
    }
}
