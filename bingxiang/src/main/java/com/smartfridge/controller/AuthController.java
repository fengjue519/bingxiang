package com.smartfridge.controller;

import com.smartfridge.entity.User;
import com.smartfridge.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        Optional<User> userOpt = userService.login(username, password);
        if (userOpt.isPresent()) {
            session.setAttribute("userId", userOpt.get().getId());
            session.setAttribute("username", userOpt.get().getUsername());
            return "redirect:/";
        }
        model.addAttribute("error", "用户名或密码错误");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam(required = false) String email,
                          Model model) {
        if (userService.register(username, password, email)) {
            model.addAttribute("success", "注册成功，请登录");
            return "login";
        }
        model.addAttribute("error", "用户名已存在");
        return "register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
