package com.pesu.bookrental.vedika.service;

import com.pesu.bookrental.vedika.dto.RentalRequestForm;
import com.pesu.bookrental.domain.enums.BookStatus;
import com.pesu.bookrental.domain.enums.PaymentStatus;
import com.pesu.bookrental.domain.enums.PaymentType;
import com.pesu.bookrental.domain.enums.RentalRequestStatus;
import com.pesu.bookrental.domain.enums.RentalStatus;
import com.pesu.bookrental.domain.model.Book;
import com.pesu.bookrental.domain.model.Payment;
import com.pesu.bookrental.domain.model.RentalRequest;
import com.pesu.bookrental.domain.model.RentalTransaction;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.factory.BookRentalFactory;
import com.pesu.bookrental.repository.BookRepository;
import com.pesu.bookrental.repository.PaymentRepository;
import com.pesu.bookrental.repository.RentalRequestRepository;
import com.pesu.bookrental.repository.RentalTransactionRepository;
import com.pesu.bookrental.tanya.service.WaitlistService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RentalService {

    private final BookRepository bookRepository;
    private final RentalRequestRepository rentalRequestRepository;
    private final RentalTransactionRepository rentalTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final WaitlistService waitlistService;
    private final BookRentalFactory bookRentalFactory;

    public RentalService(BookRepository bookRepository,
                         RentalRequestRepository rentalRequestRepository,
                         RentalTransactionRepository rentalTransactionRepository,
                         PaymentRepository paymentRepository,
                         WaitlistService waitlistService,
                         BookRentalFactory bookRentalFactory) {
        this.bookRepository = bookRepository;
        this.rentalRequestRepository = rentalRequestRepository;
        this.rentalTransactionRepository = rentalTransactionRepository;
        this.paymentRepository = paymentRepository;
        this.waitlistService = waitlistService;
        this.bookRentalFactory = bookRentalFactory;
    }

    @Transactional(readOnly = true)
    public Book getBookForRequest(Long bookId) {
        return bookRepository.findWithOwnerById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
    }

    @Transactional
    public RentalRequest createRequest(Long bookId, RentalRequestForm form, User renter) {
        Book book = getBookForRequest(bookId);

        if (book.getOwner().getId().equals(renter.getId())) {
            throw new IllegalArgumentException("You cannot request your own book.");
        }
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalArgumentException("This book is currently unavailable.");
        }
        if (form.getRequestedTo().isBefore(form.getRequestedFrom())) {
            throw new IllegalArgumentException("Requested end date cannot be before the start date.");
        }
        if (form.getRequestedFrom().isBefore(book.getAvailabilityStart())
                || form.getRequestedTo().isAfter(book.getAvailabilityEnd())) {
            throw new IllegalArgumentException("Requested dates must fall within the book's availability period.");
        }

        RentalRequest request = bookRentalFactory.createRentalRequest(
                book,
                renter,
                form.getRequestedFrom(),
                form.getRequestedTo()
        );

        book.setStatus(BookStatus.REQUESTED);
        bookRepository.save(book);
        return rentalRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<RentalRequest> findRequestsByRenter(User renter) {
        return rentalRequestRepository.findByRenterOrderByCreatedAtDesc(renter);
    }

    @Transactional(readOnly = true)
    public Map<Long, Payment> findAdvancePaymentsForRequests(List<RentalRequest> requests) {
        Map<Long, Payment> paymentsByRequestId = new LinkedHashMap<>();
        for (RentalRequest request : requests) {
            findAdvancePaymentForRequest(request).ifPresent(payment -> paymentsByRequestId.put(request.getId(), payment));
        }
        return paymentsByRequestId;
    }

    @Transactional(readOnly = true)
    public List<RentalRequest> findIncomingRequests(User owner) {
        return rentalRequestRepository.findByBookOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional
    public void approveRequest(Long requestId, User owner) {
        RentalRequest request = getRequestOwnedBy(requestId, owner);
        if (request.getStatus() != RentalRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be approved.");
        }

        request.setStatus(RentalRequestStatus.APPROVED);
        createPendingRentalForRequest(request);
        rentalRequestRepository.save(request);
    }

    @Transactional
    public void rejectRequest(Long requestId, User owner) {
        RentalRequest request = getRequestOwnedBy(requestId, owner);
        if (request.getStatus() != RentalRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be rejected.");
        }

        request.setStatus(RentalRequestStatus.REJECTED);
        Book book = request.getBook();
        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);
        waitlistService.notifyAvailability(book);
        rentalRequestRepository.save(request);
    }

    private RentalRequest getRequestOwnedBy(Long requestId, User owner) {
        RentalRequest request = rentalRequestRepository.findDetailedById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Rental request not found."));

        if (!request.getBook().getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("You can only manage requests for your own books.");
        }

        return request;
    }

    @Transactional(readOnly = true)
    public List<RentalTransaction> findBorrowedRentals(User renter) {
        List<RentalTransaction> rentals = rentalTransactionRepository.findByRenterDetailed(renter);
        refreshOverdueStatuses(rentals);
        return rentals;
    }

    @Transactional(readOnly = true)
    public List<RentalTransaction> findLentRentals(User lender) {
        List<RentalTransaction> rentals = rentalTransactionRepository.findByLenderDetailed(lender);
        refreshOverdueStatuses(rentals);
        return rentals;
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findAdvancePaymentForRequest(RentalRequest request) {
        return rentalTransactionRepository.findForRequest(request)
                .flatMap(transaction -> paymentRepository.findByRentalTransactionAndType(transaction, PaymentType.ADVANCE));
    }

    @Transactional
    public void completeAdvancePayment(Long paymentId, User renter) {
        Payment payment = paymentRepository.findDetailedById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found."));

        RentalTransaction transaction = payment.getRentalTransaction();
        if (!transaction.getRenter().getId().equals(renter.getId())) {
            throw new IllegalArgumentException("You can only pay for your own rental.");
        }
        if (payment.getType() != PaymentType.ADVANCE) {
            throw new IllegalArgumentException("This payment is not an advance payment.");
        }
        if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            throw new IllegalArgumentException("This advance payment is already completed.");
        }

        payment.setStatus(PaymentStatus.SUCCESSFUL);
        transaction.setStatus(RentalStatus.ACTIVE);
        transaction.getBook().setStatus(BookStatus.RENTED);
        paymentRepository.save(payment);
        rentalTransactionRepository.save(transaction);
        bookRepository.save(transaction.getBook());
    }

    @Transactional(readOnly = true)
    public Map<Long, Payment> findPaymentsForRentals(List<RentalTransaction> rentals) {
        Map<Long, Payment> paymentsByRentalId = new LinkedHashMap<>();
        for (RentalTransaction rental : rentals) {
            paymentRepository.findByRentalTransaction(rental).stream()
                    .filter(payment -> payment.getStatus() == PaymentStatus.PENDING || payment.getStatus() == PaymentStatus.SUCCESSFUL)
                    .sorted((left, right) -> {
                        if (left.getStatus() != right.getStatus()) {
                            return left.getStatus() == PaymentStatus.PENDING ? -1 : 1;
                        }
                        return right.getId().compareTo(left.getId());
                    })
                    .findFirst()
                    .ifPresent(payment -> paymentsByRentalId.put(rental.getId(), payment));
        }
        return paymentsByRentalId;
    }

    @Transactional
    public void requestReturn(Long rentalId, User renter) {
        RentalTransaction transaction = getRentalForRenter(rentalId, renter);
        if (transaction.getStatus() != RentalStatus.ACTIVE && transaction.getStatus() != RentalStatus.EXTENDED) {
            throw new IllegalArgumentException("Only active rentals can be marked for return.");
        }

        transaction.setStatus(RentalStatus.RETURN_PENDING);
        transaction.getBook().setStatus(BookStatus.RETURN_PENDING);
        rentalTransactionRepository.save(transaction);
        bookRepository.save(transaction.getBook());
    }

    @Transactional
    public void requestExtension(Long rentalId, LocalDate requestedDueDate, User renter) {
        RentalTransaction transaction = getRentalForRenter(rentalId, renter);
        if (transaction.getStatus() != RentalStatus.ACTIVE && transaction.getStatus() != RentalStatus.EXTENDED) {
            throw new IllegalArgumentException("Only active rentals can request an extension.");
        }
        if (requestedDueDate == null) {
            throw new IllegalArgumentException("Please choose a requested due date.");
        }
        if (!requestedDueDate.isAfter(transaction.getDueDate())) {
            throw new IllegalArgumentException("The requested due date must be after the current due date.");
        }
        if (transaction.getBook().getAvailabilityEnd() != null
                && requestedDueDate.isAfter(transaction.getBook().getAvailabilityEnd())) {
            throw new IllegalArgumentException("The requested due date exceeds the book's listed availability.");
        }

        transaction.setRequestedExtensionDueDate(requestedDueDate);
        transaction.setStatus(RentalStatus.EXTENSION_REQUESTED);
        rentalTransactionRepository.save(transaction);
    }

    @Transactional
    public void approveExtension(Long rentalId, User lender) {
        RentalTransaction transaction = getRentalForLender(rentalId, lender);
        if (transaction.getStatus() != RentalStatus.EXTENSION_REQUESTED || transaction.getRequestedExtensionDueDate() == null) {
            throw new IllegalArgumentException("There is no pending extension request for this rental.");
        }

        transaction.setDueDate(transaction.getRequestedExtensionDueDate());
        transaction.setRequestedExtensionDueDate(null);
        transaction.setStatus(RentalStatus.EXTENDED);
        rentalTransactionRepository.save(transaction);
    }

    @Transactional
    public void rejectExtension(Long rentalId, User lender) {
        RentalTransaction transaction = getRentalForLender(rentalId, lender);
        if (transaction.getStatus() != RentalStatus.EXTENSION_REQUESTED) {
            throw new IllegalArgumentException("There is no pending extension request for this rental.");
        }

        transaction.setRequestedExtensionDueDate(null);
        transaction.setStatus(RentalStatus.ACTIVE);
        rentalTransactionRepository.save(transaction);
    }

    @Transactional
    public void confirmReturn(Long rentalId, BigDecimal extraCharges, User lender) {
        RentalTransaction transaction = getRentalForLender(rentalId, lender);
        if (transaction.getStatus() != RentalStatus.RETURN_PENDING
                && transaction.getStatus() != RentalStatus.ACTIVE
                && transaction.getStatus() != RentalStatus.EXTENDED) {
            throw new IllegalArgumentException("This rental cannot be returned right now.");
        }

        BigDecimal normalizedCharges = extraCharges == null ? BigDecimal.ZERO : extraCharges.max(BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);
        transaction.setReturnDate(java.time.LocalDate.now());
        transaction.setExtraCharges(normalizedCharges);

        if (normalizedCharges.compareTo(BigDecimal.ZERO) > 0) {
            transaction.setStatus(RentalStatus.CHARGE_PENDING);
            createOutstandingChargePayment(transaction, normalizedCharges);
        } else {
            transaction.setStatus(RentalStatus.COMPLETED);
            makeBookAvailable(transaction.getBook());
        }

        rentalTransactionRepository.save(transaction);
    }

    @Transactional
    public void completeOutstandingPayment(Long paymentId, User renter) {
        Payment payment = paymentRepository.findDetailedById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found."));

        RentalTransaction transaction = payment.getRentalTransaction();
        if (!transaction.getRenter().getId().equals(renter.getId())) {
            throw new IllegalArgumentException("You can only pay charges for your own rental.");
        }
        if (payment.getType() != PaymentType.OUTSTANDING_CHARGE) {
            throw new IllegalArgumentException("This payment is not an outstanding charge.");
        }
        if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            throw new IllegalArgumentException("This charge has already been paid.");
        }

        payment.setStatus(PaymentStatus.SUCCESSFUL);
        transaction.setStatus(RentalStatus.COMPLETED);
        paymentRepository.save(payment);
        rentalTransactionRepository.save(transaction);
        makeBookAvailable(transaction.getBook());
    }

    private RentalTransaction getRentalForRenter(Long rentalId, User renter) {
        RentalTransaction transaction = rentalTransactionRepository.findDetailedById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental transaction not found."));
        if (!transaction.getRenter().getId().equals(renter.getId())) {
            throw new IllegalArgumentException("You can only manage your own borrowed rentals.");
        }
        return transaction;
    }

    private RentalTransaction getRentalForLender(Long rentalId, User lender) {
        RentalTransaction transaction = rentalTransactionRepository.findDetailedById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental transaction not found."));
        if (!transaction.getLender().getId().equals(lender.getId())) {
            throw new IllegalArgumentException("You can only manage rentals for your own books.");
        }
        return transaction;
    }

    private void createPendingRentalForRequest(RentalRequest request) {
        if (rentalTransactionRepository.findForRequest(request).isPresent()) {
            return;
        }

        RentalTransaction transaction = bookRentalFactory.createPendingRentalTransaction(request);
        RentalTransaction savedTransaction = rentalTransactionRepository.save(transaction);

        Payment payment = bookRentalFactory.createPayment(
                savedTransaction,
                PaymentType.ADVANCE,
                calculateAdvanceAmount(request.getBook().getRentalPrice())
        );
        paymentRepository.save(payment);
    }

    private void createOutstandingChargePayment(RentalTransaction transaction, BigDecimal amount) {
        Payment payment = paymentRepository.findByRentalTransactionAndType(transaction, PaymentType.OUTSTANDING_CHARGE)
                .orElseGet(() -> {
                    return bookRentalFactory.createPayment(transaction, PaymentType.OUTSTANDING_CHARGE, amount);
                });
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
    }

    private void makeBookAvailable(Book book) {
        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);
        waitlistService.notifyAvailability(book);
    }

    private void refreshOverdueStatuses(List<RentalTransaction> rentals) {
        LocalDate today = LocalDate.now();
        for (RentalTransaction rental : rentals) {
            if ((rental.getStatus() == RentalStatus.ACTIVE || rental.getStatus() == RentalStatus.EXTENDED)
                    && rental.getDueDate() != null
                    && rental.getDueDate().isBefore(today)) {
                BigDecimal overdueCharge = calculateOverdueCharge(rental.getDueDate(), today);
                rental.setExtraCharges(overdueCharge);
                rental.setStatus(RentalStatus.CHARGE_PENDING);
                createOutstandingChargePayment(rental, overdueCharge);
                rentalTransactionRepository.save(rental);
            }
        }
    }

    private BigDecimal calculateAdvanceAmount(BigDecimal rentalPrice) {
        return rentalPrice.multiply(BigDecimal.valueOf(0.30)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOverdueCharge(LocalDate dueDate, LocalDate today) {
        long lateDays = Math.max(1, ChronoUnit.DAYS.between(dueDate, today));
        return BigDecimal.valueOf(lateDays * 10L).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
