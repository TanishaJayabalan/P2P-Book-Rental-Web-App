package com.pesu.bookrental.vedika.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class RentalRequestForm {

    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestedFrom;

    @NotNull(message = "End date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestedTo;

    public LocalDate getRequestedFrom() {
        return requestedFrom;
    }

    public void setRequestedFrom(LocalDate requestedFrom) {
        this.requestedFrom = requestedFrom;
    }

    public LocalDate getRequestedTo() {
        return requestedTo;
    }

    public void setRequestedTo(LocalDate requestedTo) {
        this.requestedTo = requestedTo;
    }
}
