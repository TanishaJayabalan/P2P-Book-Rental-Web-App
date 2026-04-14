package com.pesu.bookrental.tanisha.controller;

import com.pesu.bookrental.config.CurrentUser;
import com.pesu.bookrental.tanisha.dto.ProfileUpdateForm;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.tanisha.service.AuthService;
import com.pesu.bookrental.vennela.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final AuthService authService;
    private final CurrentUser currentUser;
    private final ReviewService reviewService;

    public UserController(AuthService authService, CurrentUser currentUser, ReviewService reviewService) {
        this.authService = authService;
        this.currentUser = currentUser;
        this.reviewService = reviewService;
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to view your profile.");
            return "redirect:/login";
        }

        if (!model.containsAttribute("profileForm")) {
            ProfileUpdateForm form = new ProfileUpdateForm();
            form.setName(user.getName());
            form.setEmail(user.getEmail());
            form.setLocation(user.getLocation());
            model.addAttribute("profileForm", form);
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("receivedReviews", reviewService.findReceivedReviews(user));
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileUpdateForm profileForm,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to update your profile.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", user);
            model.addAttribute("receivedReviews", reviewService.findReceivedReviews(user));
            return "user/profile";
        }

        try {
            User updatedUser = authService.updateProfile(user.getId(), profileForm);
            model.addAttribute("currentUser", updatedUser);
            model.addAttribute("receivedReviews", reviewService.findReceivedReviews(updatedUser));
            model.addAttribute("successMessage", "Profile updated successfully.");
            return "user/profile";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("currentUser", user);
            model.addAttribute("receivedReviews", reviewService.findReceivedReviews(user));
            model.addAttribute("errorMessage", ex.getMessage());
            return "user/profile";
        }
    }

    private User getLoggedInUser(HttpSession session) {
        return currentUser.getUserId(session)
                .flatMap(authService::findById)
                .orElse(null);
    }
}
