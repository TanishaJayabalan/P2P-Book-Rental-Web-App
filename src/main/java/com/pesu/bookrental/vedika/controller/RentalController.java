package com.pesu.bookrental.vedika.controller;

import com.pesu.bookrental.config.CurrentUser;
import com.pesu.bookrental.vennela.dto.ChatMessageForm;
import com.pesu.bookrental.vedika.dto.ExtensionRequestForm;
import com.pesu.bookrental.vedika.dto.RentalRequestForm;
import com.pesu.bookrental.vennela.dto.ReviewForm;
import com.pesu.bookrental.domain.model.Book;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.tanisha.service.AuthService;
import com.pesu.bookrental.vennela.service.ChatService;
import com.pesu.bookrental.vedika.service.RentalService;
import com.pesu.bookrental.vennela.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RentalController {

    private final RentalService rentalService;
    private final AuthService authService;
    private final CurrentUser currentUser;
    private final ReviewService reviewService;
    private final ChatService chatService;

    public RentalController(RentalService rentalService,
                            AuthService authService,
                            CurrentUser currentUser,
                            ReviewService reviewService,
                            ChatService chatService) {
        this.rentalService = rentalService;
        this.authService = authService;
        this.currentUser = currentUser;
        this.reviewService = reviewService;
        this.chatService = chatService;
    }

    @GetMapping("/books/{bookId}/request")
    public String requestPage(@PathVariable Long bookId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to request a book.");
            return "redirect:/login";
        }

        Book book;
        try {
            book = rentalService.getBookForRequest(bookId);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/books";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("book", book);
        if (!model.containsAttribute("rentalRequestForm")) {
            RentalRequestForm form = new RentalRequestForm();
            form.setRequestedFrom(book.getAvailabilityStart() != null ? book.getAvailabilityStart() : LocalDate.now());
            form.setRequestedTo(book.getAvailabilityEnd() != null ? book.getAvailabilityEnd() : LocalDate.now().plusDays(7));
            model.addAttribute("rentalRequestForm", form);
        }
        return "rentals/request";
    }

    @PostMapping("/books/{bookId}/request")
    public String createRequest(@PathVariable Long bookId,
                                @Valid @ModelAttribute("rentalRequestForm") RentalRequestForm rentalRequestForm,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to request a book.");
            return "redirect:/login";
        }

        Book book;
        try {
            book = rentalService.getBookForRequest(bookId);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/books";
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("book", book);

        if (bindingResult.hasErrors()) {
            return "rentals/request";
        }

        try {
            rentalService.createRequest(bookId, rentalRequestForm, user);
            redirectAttributes.addFlashAttribute("successMessage", "Rental request submitted successfully.");
            return "redirect:/my-requests";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "rentals/request";
        }
    }

    @GetMapping("/my-requests")
    public String myRequests(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to view your requests.");
            return "redirect:/login";
        }

        var requests = rentalService.findRequestsByRenter(user);
        model.addAttribute("currentUser", user);
        model.addAttribute("requests", requests);
        model.addAttribute("advancePayments", rentalService.findAdvancePaymentsForRequests(requests));
        return "rentals/my-requests";
    }

    @GetMapping("/incoming-requests")
    public String incomingRequests(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to manage incoming requests.");
            return "redirect:/login";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("requests", rentalService.findIncomingRequests(user));
        return "rentals/incoming-requests";
    }

    @PostMapping("/requests/{requestId}/approve")
    public String approveRequest(@PathVariable Long requestId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to manage rental requests.");
            return "redirect:/login";
        }

        try {
            rentalService.approveRequest(requestId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Rental request approved.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/incoming-requests";
    }

    @PostMapping("/requests/{requestId}/reject")
    public String rejectRequest(@PathVariable Long requestId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to manage rental requests.");
            return "redirect:/login";
        }

        try {
            rentalService.rejectRequest(requestId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Rental request rejected.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/incoming-requests";
    }

    @PostMapping("/payments/{paymentId}/complete")
    public String completeAdvancePayment(@PathVariable Long paymentId,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to complete payment.");
            return "redirect:/login";
        }

        try {
            rentalService.completeAdvancePayment(paymentId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Advance payment completed. Your rental is now active.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/my-requests";
    }

    @GetMapping("/rentals")
    public String rentalsDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to view your rentals.");
            return "redirect:/login";
        }

        var borrowed = rentalService.findBorrowedRentals(user);
        var lent = rentalService.findLentRentals(user);

        model.addAttribute("currentUser", user);
        model.addAttribute("borrowedRentals", borrowed);
        model.addAttribute("lentRentals", lent);
        model.addAttribute("rentalPayments", rentalService.findPaymentsForRentals(borrowed));
        model.addAttribute("reviewForm", new ReviewForm());
        model.addAttribute("extensionRequestForm", new ExtensionRequestForm());
        return "rentals/dashboard";
    }

    @PostMapping("/rentals/{rentalId}/request-extension")
    public String requestExtension(@PathVariable Long rentalId,
                                   @Valid @ModelAttribute("extensionRequestForm") ExtensionRequestForm extensionRequestForm,
                                   BindingResult bindingResult,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to manage rentals.");
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please choose a valid requested due date.");
            return "redirect:/rentals";
        }

        try {
            rentalService.requestExtension(rentalId, extensionRequestForm.getRequestedDueDate(), user);
            redirectAttributes.addFlashAttribute("successMessage", "Extension request sent to the lender.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rentals";
    }

    @PostMapping("/rentals/{rentalId}/approve-extension")
    public String approveExtension(@PathVariable Long rentalId,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to manage rentals.");
            return "redirect:/login";
        }

        try {
            rentalService.approveExtension(rentalId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Extension request approved.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rentals";
    }

    @PostMapping("/rentals/{rentalId}/reject-extension")
    public String rejectExtension(@PathVariable Long rentalId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to manage rentals.");
            return "redirect:/login";
        }

        try {
            rentalService.rejectExtension(rentalId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Extension request rejected.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rentals";
    }

    @PostMapping("/rentals/{rentalId}/return-request")
    public String requestReturn(@PathVariable Long rentalId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to manage rentals.");
            return "redirect:/login";
        }

        try {
            rentalService.requestReturn(rentalId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Return request submitted to the lender.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rentals";
    }

    @PostMapping("/rentals/{rentalId}/confirm-return")
    public String confirmReturn(@PathVariable Long rentalId,
                                @org.springframework.web.bind.annotation.RequestParam(name = "extraCharges", defaultValue = "0") BigDecimal extraCharges,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to manage rentals.");
            return "redirect:/login";
        }

        try {
            rentalService.confirmReturn(rentalId, extraCharges, user);
            if (extraCharges != null && extraCharges.compareTo(BigDecimal.ZERO) > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "Return confirmed. Outstanding charges were added for the renter.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Return confirmed and rental completed.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rentals";
    }

    @PostMapping("/payments/{paymentId}/settle-charge")
    public String settleOutstandingCharge(@PathVariable Long paymentId,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to complete payment.");
            return "redirect:/login";
        }

        try {
            rentalService.completeOutstandingPayment(paymentId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Outstanding charges paid. Rental is now completed.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rentals";
    }

    @PostMapping("/rentals/{rentalId}/review")
    public String submitReview(@PathVariable Long rentalId,
                               @Valid @ModelAttribute("reviewForm") ReviewForm reviewForm,
                               BindingResult bindingResult,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to submit a review.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a rating between 1 and 5.");
            return "redirect:/rentals";
        }

        try {
            reviewService.submitReview(rentalId, reviewForm, user);
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rentals";
    }

    @GetMapping("/rentals/{rentalId}/chat")
    public String rentalChat(@PathVariable Long rentalId,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to access chat.");
            return "redirect:/login";
        }

        try {
            var rental = chatService.getRentalForConversation(rentalId, user);
            model.addAttribute("currentUser", user);
            model.addAttribute("rental", rental);
            model.addAttribute("messages", chatService.findConversation(rentalId, user));
            if (!model.containsAttribute("chatMessageForm")) {
                model.addAttribute("chatMessageForm", new ChatMessageForm());
            }
            return "rentals/chat";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/rentals";
        }
    }

    @PostMapping("/rentals/{rentalId}/chat")
    public String sendChatMessage(@PathVariable Long rentalId,
                                  @Valid @ModelAttribute("chatMessageForm") ChatMessageForm chatMessageForm,
                                  BindingResult bindingResult,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to access chat.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            try {
                var rental = chatService.getRentalForConversation(rentalId, user);
                model.addAttribute("currentUser", user);
                model.addAttribute("rental", rental);
                model.addAttribute("messages", chatService.findConversation(rentalId, user));
                return "rentals/chat";
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                return "redirect:/rentals";
            }
        }

        try {
            chatService.sendMessage(rentalId, chatMessageForm, user);
            redirectAttributes.addFlashAttribute("successMessage", "Message sent.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rentals/" + rentalId + "/chat";
    }

    private User getLoggedInUser(HttpSession session) {
        return currentUser.getUserId(session)
                .flatMap(authService::findById)
                .orElse(null);
    }
}
