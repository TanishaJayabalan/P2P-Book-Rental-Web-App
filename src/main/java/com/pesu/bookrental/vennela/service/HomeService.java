package com.pesu.bookrental.vennela.service;

import com.pesu.bookrental.domain.enums.BookStatus;
import com.pesu.bookrental.domain.enums.RentalStatus;
import com.pesu.bookrental.repository.NotificationRepository;
import com.pesu.bookrental.repository.BookRepository;
import com.pesu.bookrental.repository.RentalRequestRepository;
import com.pesu.bookrental.repository.RentalTransactionRepository;
import com.pesu.bookrental.repository.ReviewRepository;
import com.pesu.bookrental.repository.UserRepository;
import com.pesu.bookrental.repository.WaitlistRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class HomeService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final RentalRequestRepository rentalRequestRepository;
    private final RentalTransactionRepository rentalTransactionRepository;
    private final ReviewRepository reviewRepository;
    private final WaitlistRepository waitlistRepository;
    private final NotificationRepository notificationRepository;

    public HomeService(UserRepository userRepository,
                       BookRepository bookRepository,
                       RentalRequestRepository rentalRequestRepository,
                       RentalTransactionRepository rentalTransactionRepository,
                       ReviewRepository reviewRepository,
                       WaitlistRepository waitlistRepository,
                       NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.rentalRequestRepository = rentalRequestRepository;
        this.rentalTransactionRepository = rentalTransactionRepository;
        this.reviewRepository = reviewRepository;
        this.waitlistRepository = waitlistRepository;
        this.notificationRepository = notificationRepository;
    }

    public Map<String, Long> getPlatformStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("Registered Users", userRepository.count());
        stats.put("Available Books", (long) bookRepository.findByStatus(BookStatus.AVAILABLE).size());
        stats.put("Rental Requests", rentalRequestRepository.count());
        stats.put("Active Rentals", rentalTransactionRepository.findAll().stream()
                .filter(rental -> rental.getStatus() == RentalStatus.ACTIVE
                        || rental.getStatus() == RentalStatus.EXTENDED
                        || rental.getStatus() == RentalStatus.EXTENSION_REQUESTED)
                .count());
        stats.put("Completed Rentals", rentalTransactionRepository.findAll().stream()
                .filter(rental -> rental.getStatus() == RentalStatus.COMPLETED)
                .count());
        return stats;
    }

    public Map<String, String> getAdminAnalytics() {
        Map<String, String> analytics = new LinkedHashMap<>();
        long totalBooks = bookRepository.count();
        long unavailableBooks = rentalTransactionRepository.findAll().stream()
                .filter(rental -> rental.getStatus() == RentalStatus.ACTIVE
                        || rental.getStatus() == RentalStatus.EXTENDED
                        || rental.getStatus() == RentalStatus.RETURN_PENDING
                        || rental.getStatus() == RentalStatus.CHARGE_PENDING
                        || rental.getStatus() == RentalStatus.EXTENSION_REQUESTED)
                .count();
        long overdueRentals = rentalTransactionRepository.findAll().stream()
                .filter(rental -> rental.getStatus() == RentalStatus.CHARGE_PENDING)
                .count();
        double averageRating = userRepository.findAll().stream()
                .mapToDouble(user -> user.getRatingAverage() == null ? 0.0 : user.getRatingAverage())
                .filter(rating -> rating > 0)
                .average()
                .orElse(0.0);

        analytics.put("Total Book Listings", String.valueOf(totalBooks));
        analytics.put("Unavailable Listings", String.valueOf(unavailableBooks));
        analytics.put("Active Waitlist Entries", String.valueOf(waitlistRepository.countByActiveTrue()));
        analytics.put("Reviews Submitted", String.valueOf(reviewRepository.count()));
        analytics.put("System Notifications", String.valueOf(notificationRepository.count()));
        analytics.put("Overdue Rentals", String.valueOf(overdueRentals));
        analytics.put("Average User Rating", String.format("%.2f", averageRating));
        return analytics;
    }
}
