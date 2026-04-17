package com.pesu.bookrental.tanya.service;

import com.pesu.bookrental.domain.enums.BookStatus;
import com.pesu.bookrental.domain.model.Book;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.domain.model.WaitlistEntry;
import com.pesu.bookrental.factory.BookRentalFactory;
import com.pesu.bookrental.repository.BookRepository;
import com.pesu.bookrental.repository.WaitlistRepository;
import com.pesu.bookrental.tanisha.service.NotificationService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final BookRepository bookRepository;
    private final NotificationService notificationService;
    private final BookRentalFactory bookRentalFactory;

    public WaitlistService(WaitlistRepository waitlistRepository,
                           BookRepository bookRepository,
                           NotificationService notificationService,
                           BookRentalFactory bookRentalFactory) {
        this.waitlistRepository = waitlistRepository;
        this.bookRepository = bookRepository;
        this.notificationService = notificationService;
        this.bookRentalFactory = bookRentalFactory;
    }

    @Transactional
    public WaitlistEntry joinWaitlist(Long bookId, User user) {
        Book book = bookRepository.findWithOwnerById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));

        if (book.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot join the waitlist for your own book.");
        }
        if (book.getStatus() == BookStatus.AVAILABLE) {
            throw new IllegalArgumentException("This book is currently available, so you can request it directly.");
        }
        if (waitlistRepository.findByBookAndUserAndActiveTrue(book, user).isPresent()) {
            throw new IllegalArgumentException("You are already on the waitlist for this book.");
        }

        WaitlistEntry entry = bookRentalFactory.createWaitlistEntry(book, user);
        return waitlistRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<WaitlistEntry> findMyActiveEntries(User user) {
        return waitlistRepository.findActiveByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Long> findActiveUserIdsForBook(Long bookId) {
        return waitlistRepository.findActiveUserIdsByBookId(bookId);
    }

    @Transactional
    public void notifyAvailability(Book book) {
        List<WaitlistEntry> entries = waitlistRepository.findActiveEntriesByBook(book);
        for (WaitlistEntry entry : entries) {
            notificationService.createNotification(
                    entry.getUser(),
                    "The book \"" + book.getTitle() + "\" is available again. You can request it now."
            );
            entry.setActive(false);
        }
        waitlistRepository.saveAll(entries);
    }
}
