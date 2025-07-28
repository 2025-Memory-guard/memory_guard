package com.example.memory_guard.global;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    @GetMapping("/user/login")
    public String userLogin() {
        return "user-login";
    }

    @GetMapping("/guard/login")
    public String guardLogin() {
        return "guard-login";
    }
}