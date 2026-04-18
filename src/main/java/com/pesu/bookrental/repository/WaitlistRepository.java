package com.pesu.bookrental.repository;

import com.pesu.bookrental.domain.model.Book;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.domain.model.WaitlistEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    long countByActiveTrue();

    Optional<WaitlistEntry> findByBookAndUserAndActiveTrue(Book book, User user);

    @Query("""
            select w from WaitlistEntry w
            join fetch w.book b
            join fetch b.owner o
            where w.user = :user and w.active = true
            order by w.createdAt desc
            """)
    List<WaitlistEntry> findActiveByUser(@Param("user") User user);

    @Query("""
            select w.user.id from WaitlistEntry w
            where w.book.id = :bookId and w.active = true
            """)
    List<Long> findActiveUserIdsByBookId(@Param("bookId") Long bookId);

    @Query("""
            select w from WaitlistEntry w
            join fetch w.user u
            join fetch w.book b
            join fetch b.owner o
            where w.book = :book and w.active = true
            order by w.createdAt asc
            """)
    List<WaitlistEntry> findActiveEntriesByBook(@Param("book") Book book);
}
