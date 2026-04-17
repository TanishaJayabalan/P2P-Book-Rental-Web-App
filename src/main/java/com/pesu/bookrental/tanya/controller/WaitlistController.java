package com.pesu.bookrental.tanya.controller;

import com.pesu.bookrental.config.CurrentUser;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.tanisha.service.AuthService;
import com.pesu.bookrental.tanya.service.WaitlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WaitlistController {

    private final WaitlistService waitlistService;
    private final AuthService authService;
    private final CurrentUser currentUser;

    public WaitlistController(WaitlistService waitlistService, AuthService authService, CurrentUser currentUser) {
        this.waitlistService = waitlistService;
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/books/{bookId}/waitlist")
    public String joinWaitlist(@PathVariable Long bookId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to join the waitlist.");
            return "redirect:/login";
        }

        try {
            waitlistService.joinWaitlist(bookId, user);
            redirectAttributes.addFlashAttribute("successMessage", "You were added to the waitlist for this book.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/waitlist")
    public String myWaitlist(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to view your waitlist.");
            return "redirect:/login";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("entries", waitlistService.findMyActiveEntries(user));
        return "waitlist/list";
    }

    private User getLoggedInUser(HttpSession session) {
        return currentUser.getUserId(session)
                .flatMap(authService::findById)
                .orElse(null);
    }
}
