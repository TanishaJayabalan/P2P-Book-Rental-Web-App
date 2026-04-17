package com.pesu.bookrental.tanya.service;

import com.pesu.bookrental.tanya.dto.BookForm;
import com.pesu.bookrental.domain.enums.BookStatus;
import com.pesu.bookrental.domain.model.Book;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.repository.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Book createListing(BookForm form, User owner) {
        if (form.getAvailabilityEnd().isBefore(form.getAvailabilityStart())) {
            throw new IllegalArgumentException("Availability end date cannot be before the start date.");
        }

        Book book = new Book();
        book.setTitle(form.getTitle().trim());
        book.setAuthor(form.getAuthor().trim());
        book.setGenre(form.getGenre().trim());
        book.setConditionDescription(form.getConditionDescription().trim());
        book.setRentalPrice(form.getRentalPrice());
        book.setAvailabilityStart(form.getAvailabilityStart());
        book.setAvailabilityEnd(form.getAvailabilityEnd());
        book.setStatus(BookStatus.AVAILABLE);
        book.setOwner(owner);
        return bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isBlank()) {
            return bookRepository.findAllWithOwnerOrderByIdDesc();
        }
        return bookRepository.searchAllBooks(normalizedQuery);
    }
}
