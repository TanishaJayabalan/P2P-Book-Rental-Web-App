package com.pesu.bookrental.repository;

import com.pesu.bookrental.domain.enums.BookStatus;
import com.pesu.bookrental.domain.model.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByStatus(BookStatus status);

    List<Book> findByStatusOrderByIdDesc(BookStatus status);

    @Query("""
            select b from Book b
            join fetch b.owner
            order by b.id desc
            """)
    List<Book> findAllWithOwnerOrderByIdDesc();

    @Query("""
            select b from Book b
            join fetch b.owner
            where b.id = :bookId
            """)
    Optional<Book> findWithOwnerById(@Param("bookId") Long bookId);

    @Query("""
            select b from Book b
            where b.status = :status
            and (
                lower(b.title) like lower(concat('%', :query, '%'))
                or lower(b.author) like lower(concat('%', :query, '%'))
                or lower(b.genre) like lower(concat('%', :query, '%'))
            )
            order by b.id desc
            """)
    List<Book> searchAvailableBooks(@Param("status") BookStatus status, @Param("query") String query);

    @Query("""
            select b from Book b
            join fetch b.owner
            where lower(b.title) like lower(concat('%', :query, '%'))
               or lower(b.author) like lower(concat('%', :query, '%'))
               or lower(b.genre) like lower(concat('%', :query, '%'))
            order by b.id desc
            """)
    List<Book> searchAllBooks(@Param("query") String query);
}
