package com.shemhazaicraft.helloauth.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "redirect:http://localhost:3000/login";
    }

}
