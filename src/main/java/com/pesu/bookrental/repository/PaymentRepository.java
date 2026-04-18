package com.pesu.bookrental.repository;

import com.pesu.bookrental.domain.enums.PaymentType;
import com.pesu.bookrental.domain.model.Payment;
import com.pesu.bookrental.domain.model.RentalTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
            select p from Payment p
            join fetch p.rentalTransaction rt
            join fetch rt.book b
            join fetch rt.lender l
            join fetch rt.renter r
            where p.id = :paymentId
            """)
    Optional<Payment> findDetailedById(@Param("paymentId") Long paymentId);

    Optional<Payment> findByRentalTransactionAndType(RentalTransaction rentalTransaction, PaymentType type);

    List<Payment> findByRentalTransaction(RentalTransaction rentalTransaction);
}
