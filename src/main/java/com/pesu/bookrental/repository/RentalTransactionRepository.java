package com.pesu.bookrental.repository;

import com.pesu.bookrental.domain.model.RentalRequest;
import com.pesu.bookrental.domain.model.RentalTransaction;
import com.pesu.bookrental.domain.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalTransactionRepository extends JpaRepository<RentalTransaction, Long> {

    @Query("""
            select rt from RentalTransaction rt
            join fetch rt.book b
            join fetch rt.lender l
            join fetch rt.renter r
            where rt.id = :transactionId
            """)
    Optional<RentalTransaction> findDetailedById(@Param("transactionId") Long transactionId);

    @Query("""
            select rt from RentalTransaction rt
            where rt.book = :#{#request.book}
              and rt.renter = :#{#request.renter}
              and rt.startDate = :#{#request.requestedFrom}
              and rt.dueDate = :#{#request.requestedTo}
            """)
    Optional<RentalTransaction> findForRequest(@Param("request") RentalRequest request);

    @Query("""
            select rt from RentalTransaction rt
            join fetch rt.book b
            join fetch rt.lender l
            join fetch rt.renter r
            where rt.renter = :renter
            order by rt.id desc
            """)
    List<RentalTransaction> findByRenterDetailed(@Param("renter") User renter);

    @Query("""
            select rt from RentalTransaction rt
            join fetch rt.book b
            join fetch rt.lender l
            join fetch rt.renter r
            where rt.lender = :lender
            order by rt.id desc
            """)
    List<RentalTransaction> findByLenderDetailed(@Param("lender") User lender);
}
