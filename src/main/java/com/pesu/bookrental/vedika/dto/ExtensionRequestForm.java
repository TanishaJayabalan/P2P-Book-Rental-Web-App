package com.pesu.bookrental.vedika.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ExtensionRequestForm {

    @NotNull(message = "Please choose the requested extended due date.")
    private LocalDate requestedDueDate;

    public LocalDate getRequestedDueDate() {
        return requestedDueDate;
    }

    public void setRequestedDueDate(LocalDate requestedDueDate) {
        this.requestedDueDate = requestedDueDate;
    }
}
