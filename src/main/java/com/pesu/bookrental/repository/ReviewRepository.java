package com.pesu.bookrental.repository;

import com.pesu.bookrental.domain.model.RentalTransaction;
import com.pesu.bookrental.domain.model.Review;
import com.pesu.bookrental.domain.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
            select r from Review r
            join fetch r.reviewer reviewer
            join fetch r.reviewedUser reviewed
            join fetch r.rentalTransaction rt
            where r.reviewedUser = :user
            order by r.id desc
            """)
    List<Review> findReceivedReviews(@Param("user") User user);

    Optional<Review> findByRentalTransactionAndReviewer(RentalTransaction rentalTransaction, User reviewer);
}
