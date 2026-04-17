package com.pesu.bookrental.tanya.controller;

import com.pesu.bookrental.config.CurrentUser;
import com.pesu.bookrental.tanya.dto.BookForm;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.tanisha.service.AuthService;
import com.pesu.bookrental.tanya.service.BookService;
import com.pesu.bookrental.tanya.service.WaitlistService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookController {

    private final BookService bookService;
    private final AuthService authService;
    private final CurrentUser currentUser;
    private final WaitlistService waitlistService;

    public BookController(BookService bookService,
                          AuthService authService,
                          CurrentUser currentUser,
                          WaitlistService waitlistService) {
        this.bookService = bookService;
        this.authService = authService;
        this.currentUser = currentUser;
        this.waitlistService = waitlistService;
    }

    @GetMapping("/books")
    public String browseBooks(@RequestParam(name = "q", defaultValue = "") String query,
                              HttpSession session,
                              Model model) {
        User user = getLoggedInUser(session);
        model.addAttribute("currentUser", user);
        model.addAttribute("searchQuery", query);
        model.addAttribute("books", bookService.searchBooks(query));
        model.addAttribute("waitlistedBookIds",
                user == null ? List.of() : waitlistService.findMyActiveEntries(user).stream().map(entry -> entry.getBook().getId()).toList());
        return "books/list";
    }

    @GetMapping("/books/new")
    public String newBookPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to list a book.");
            return "redirect:/login";
        }

        model.addAttribute("currentUser", user);
        if (!model.containsAttribute("bookForm")) {
            model.addAttribute("bookForm", new BookForm());
        }
        return "books/new";
    }

    @PostMapping("/books")
    public String createBook(@Valid @ModelAttribute("bookForm") BookForm bookForm,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to list a book.");
            return "redirect:/login";
        }

        model.addAttribute("currentUser", user);
        if (bindingResult.hasErrors()) {
            return "books/new";
        }

        try {
            bookService.createListing(bookForm, user);
            redirectAttributes.addFlashAttribute("successMessage", "Book listed successfully.");
            return "redirect:/books";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "books/new";
        }
    }

    private User getLoggedInUser(HttpSession session) {
        return currentUser.getUserId(session)
                .flatMap(authService::findById)
                .orElse(null);
    }
}
