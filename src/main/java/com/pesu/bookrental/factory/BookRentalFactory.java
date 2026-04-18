package com.pesu.bookrental.factory;

import com.pesu.bookrental.domain.enums.PaymentStatus;
import com.pesu.bookrental.domain.enums.PaymentType;
import com.pesu.bookrental.domain.enums.RentalRequestStatus;
import com.pesu.bookrental.domain.enums.RentalStatus;
import com.pesu.bookrental.domain.enums.ReportStatus;
import com.pesu.bookrental.domain.model.Book;
import com.pesu.bookrental.domain.model.Notification;
import com.pesu.bookrental.domain.model.Payment;
import com.pesu.bookrental.domain.model.RentalRequest;
import com.pesu.bookrental.domain.model.RentalTransaction;
import com.pesu.bookrental.domain.model.Report;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.domain.model.WaitlistEntry;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class BookRentalFactory {

    public RentalRequest createRentalRequest(Book book, User renter, java.time.LocalDate requestedFrom, java.time.LocalDate requestedTo) {
        RentalRequest request = new RentalRequest();
        request.setBook(book);
        request.setRenter(renter);
        request.setRequestedFrom(requestedFrom);
        request.setRequestedTo(requestedTo);
        request.setStatus(RentalRequestStatus.PENDING);
        return request;
    }

    public RentalTransaction createPendingRentalTransaction(RentalRequest request) {
        RentalTransaction transaction = new RentalTransaction();
        transaction.setBook(request.getBook());
        transaction.setLender(request.getBook().getOwner());
        transaction.setRenter(request.getRenter());
        transaction.setStartDate(request.getRequestedFrom());
        transaction.setDueDate(request.getRequestedTo());
        transaction.setStatus(RentalStatus.PAYMENT_PENDING);
        transaction.setExtraCharges(BigDecimal.ZERO);
        return transaction;
    }

    public Payment createPayment(RentalTransaction rentalTransaction, PaymentType paymentType, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setRentalTransaction(rentalTransaction);
        payment.setType(paymentType);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    public WaitlistEntry createWaitlistEntry(Book book, User user) {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setBook(book);
        entry.setUser(user);
        entry.setActive(true);
        return entry;
    }

    public Report createReport(String reportType, String description, User reportedBy, User targetUser) {
        Report report = new Report();
        report.setReportType(reportType);
        report.setDescription(description);
        report.setReportedBy(reportedBy);
        report.setTargetUser(targetUser);
        report.setStatus(ReportStatus.OPEN);
        return report;
    }

    public Notification createNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setRead(false);
        return notification;
    }
}
