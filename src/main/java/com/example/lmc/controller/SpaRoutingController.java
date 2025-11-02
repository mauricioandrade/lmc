package com.example.lmc.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaRoutingController {

    @GetMapping(value = {
            "/",
            "/{path:^(?!api$|swagger-ui$|v3$)[^\\.]*$}",
            "/**/{path:^(?!api$|swagger-ui$|v3$)[^\\.]*$}"
    }, produces = MediaType.TEXT_HTML_VALUE)
    public String forwardSpaRequests() {
        return "forward:/index.html";
    }

}
