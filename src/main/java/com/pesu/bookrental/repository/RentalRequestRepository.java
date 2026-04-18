package com.pesu.bookrental.repository;

import com.pesu.bookrental.domain.model.RentalRequest;
import com.pesu.bookrental.domain.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {

    @Query("""
            select rr from RentalRequest rr
            join fetch rr.book b
            join fetch b.owner o
            join fetch rr.renter r
            where rr.renter = :renter
            order by rr.createdAt desc
            """)
    List<RentalRequest> findByRenterOrderByCreatedAtDesc(@Param("renter") User renter);

    @Query("""
            select rr from RentalRequest rr
            join fetch rr.book b
            join fetch b.owner o
            join fetch rr.renter r
            where b.owner = :owner
            order by rr.createdAt desc
            """)
    List<RentalRequest> findByBookOwnerOrderByCreatedAtDesc(@Param("owner") User owner);

    @Query("""
            select rr from RentalRequest rr
            join fetch rr.book b
            join fetch b.owner o
            join fetch rr.renter r
            where rr.id = :requestId
            """)
    Optional<RentalRequest> findDetailedById(@Param("requestId") Long requestId);
}
