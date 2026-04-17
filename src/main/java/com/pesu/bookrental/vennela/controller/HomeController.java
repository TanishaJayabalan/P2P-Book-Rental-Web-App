package com.pesu.bookrental.vennela.controller;

import com.pesu.bookrental.config.CurrentUser;
import com.pesu.bookrental.tanisha.service.AuthService;
import com.pesu.bookrental.vennela.service.HomeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final HomeService homeService;
    private final CurrentUser currentUser;
    private final AuthService authService;

    public HomeController(HomeService homeService, CurrentUser currentUser, AuthService authService) {
        this.homeService = homeService;
        this.currentUser = currentUser;
        this.authService = authService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("stats", homeService.getPlatformStats());
        currentUser.getUserId(session)
                .flatMap(authService::findById)
                .ifPresent(user -> model.addAttribute("currentUser", user));
        return "index";
    }
}
