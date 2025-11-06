package com.example.lmc.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardingController {

    private static final String ERROR_STATUS_ATTRIBUTE = "jakarta.servlet.error.status_code";
    private static final String INDEX_FORWARD = "forward:/";
    private static final String INTERNAL_ERROR_VIEW = "error-500";
    private static final String DEFAULT_ERROR_VIEW = "error";

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Integer statusCode = extrairStatus(request);
        if (statusCode == null) {
            return DEFAULT_ERROR_VIEW;
        }

        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == HttpStatus.NOT_FOUND) {
            return INDEX_FORWARD;
        }
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            return INTERNAL_ERROR_VIEW;
        }
        return DEFAULT_ERROR_VIEW;
    }

    private Integer extrairStatus(HttpServletRequest request) {
        Object status = request.getAttribute(ERROR_STATUS_ATTRIBUTE);
        if (status == null) {
            return null;
        }
        if (status instanceof Integer integerStatus) {
            return integerStatus;
        }
        try {
            return Integer.valueOf(status.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

