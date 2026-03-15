package com.project.dualaccesscontrol.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/api")
    public String home() {
        return "Welcome to Dual Access Control System API. Please use /api/auth/login or /api/auth/register to get an access token.";
    }
}
