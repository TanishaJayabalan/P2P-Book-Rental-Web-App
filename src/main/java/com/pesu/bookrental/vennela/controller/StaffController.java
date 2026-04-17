package com.pesu.bookrental.vennela.controller;

import com.pesu.bookrental.config.CurrentUser;
import com.pesu.bookrental.vennela.command.MarkReportUnderReviewCommand;
import com.pesu.bookrental.vennela.command.ResolveReportCommand;
import com.pesu.bookrental.vennela.dto.ReportForm;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.tanisha.service.AuthService;
import com.pesu.bookrental.vennela.facade.AdminDashboardFacade;
import com.pesu.bookrental.vennela.service.ReportService;
import com.pesu.bookrental.vennela.service.RoleAccessService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StaffController {

    private final AuthService authService;
    private final CurrentUser currentUser;
    private final ReportService reportService;
    private final RoleAccessService roleAccessService;
    private final AdminDashboardFacade adminDashboardFacade;
    private final MarkReportUnderReviewCommand markReportUnderReviewCommand;
    private final ResolveReportCommand resolveReportCommand;

    public StaffController(AuthService authService,
                           CurrentUser currentUser,
                           ReportService reportService,
                           RoleAccessService roleAccessService,
                           AdminDashboardFacade adminDashboardFacade,
                           MarkReportUnderReviewCommand markReportUnderReviewCommand,
                           ResolveReportCommand resolveReportCommand) {
        this.authService = authService;
        this.currentUser = currentUser;
        this.reportService = reportService;
        this.roleAccessService = roleAccessService;
        this.adminDashboardFacade = adminDashboardFacade;
        this.markReportUnderReviewCommand = markReportUnderReviewCommand;
        this.resolveReportCommand = resolveReportCommand;
    }

    @GetMapping("/reports/new")
    public String newReport(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to report an issue.");
            return "redirect:/login";
        }

        model.addAttribute("currentUser", user);
        if (!model.containsAttribute("reportForm")) {
            model.addAttribute("reportForm", new ReportForm());
        }
        return "reports/new";
    }

    @PostMapping("/reports")
    public String createReport(@Valid @ModelAttribute("reportForm") ReportForm reportForm,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to report an issue.");
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", user);
            return "reports/new";
        }

        reportService.createReport(reportForm, user, null);
        redirectAttributes.addFlashAttribute("successMessage", "Your report was submitted successfully.");
        return "redirect:/";
    }

    @GetMapping("/moderation")
    public String moderationDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        try {
            roleAccessService.requireModeratorOrAdmin(user);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("reports", reportService.findAllReports());
        model.addAttribute("reportStats", reportService.getReportStats());
        return "staff/moderation";
    }

    @PostMapping("/moderation/reports/{reportId}/review")
    public String markUnderReview(@PathVariable Long reportId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        try {
            roleAccessService.requireModeratorOrAdmin(user);
            markReportUnderReviewCommand.execute(reportId);
            redirectAttributes.addFlashAttribute("successMessage", "Report marked under review.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/moderation";
    }

    @PostMapping("/moderation/reports/{reportId}/resolve")
    public String resolveReport(@PathVariable Long reportId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        try {
            roleAccessService.requireModeratorOrAdmin(user);
            resolveReportCommand.execute(reportId);
            redirectAttributes.addFlashAttribute("successMessage", "Report resolved.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/moderation";
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        try {
            roleAccessService.requireAdmin(user);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("stats", adminDashboardFacade.getPlatformStats());
        model.addAttribute("analytics", adminDashboardFacade.getAnalytics());
        model.addAttribute("reportStats", adminDashboardFacade.getReportStats());
        model.addAttribute("reports", adminDashboardFacade.getReports());
        return "staff/admin";
    }

    private User getLoggedInUser(HttpSession session) {
        return currentUser.getUserId(session)
                .flatMap(authService::findById)
                .orElse(null);
    }
}
