package com.pesu.bookrental.tanisha.controller;

import com.pesu.bookrental.config.CurrentUser;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.tanisha.service.AuthService;
import com.pesu.bookrental.tanisha.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;
    private final CurrentUser currentUser;

    public NotificationController(NotificationService notificationService,
                                  AuthService authService,
                                  CurrentUser currentUser) {
        this.notificationService = notificationService;
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to view notifications.");
            return "redirect:/login";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("notifications", notificationService.findNotifications(user));
        model.addAttribute("unreadCount", notificationService.countUnreadNotifications(user));
        return "notifications/list";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to update notifications.");
            return "redirect:/login";
        }

        notificationService.markAllRead(user);
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/notifications";
    }

    private User getLoggedInUser(HttpSession session) {
        return currentUser.getUserId(session)
                .flatMap(authService::findById)
                .orElse(null);
    }
}
