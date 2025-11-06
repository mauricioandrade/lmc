package com.example.lmc.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardingController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            // Se for um 404 (Not Found), encaminha para o index.html
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                // Encaminha para a raiz, que serve o index.html
                return "forward:/";
            }
            // Se for outro erro (ex: 500), deixa o Spring mostrar a página de erro
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-500"; // (Você pode criar uma página error-500.html se quiser)
            }
        }

        // Página de erro padrão
        return "error";
    }
}

