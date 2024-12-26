package com.example.rest_api.controller;

import com.example.rest_api.database.model.UserEntity;
import com.example.rest_api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/login")
public class LoginController {
    private UserService userService;
    private Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public String loadLoginPage(Model model){
        model.addAttribute("user", new UserEntity());
        return "authentication/login";
    }
}
