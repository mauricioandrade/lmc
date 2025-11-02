package com.example.lmc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaRoutingController {

    @RequestMapping(value = {
            "/",
            "/{path:[^\\.]*}",
            "/{path:.*}"
    })  // Added closing brace here
    public String forwardSpaRequests() {
        return "forward:/index.html";
    }

}
