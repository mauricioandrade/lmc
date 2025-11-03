package com.example.lmc.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SpaRoutingController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/{*path}")
    public String forwardSpaRequests(@PathVariable String path) {

        if (path.contains(".")) {
            return "forward:/" + path;
        }

        if (path.startsWith("api/") || path.startsWith("v3/") || path.startsWith("swagger-ui/")) {
            return "forward:/" + path;
        }

        return "forward:/index.html";
    }
}
