package com.pesu.bookrental.tanya.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class BookForm {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "Genre is required")
    private String genre;

    @NotBlank(message = "Condition is required")
    private String conditionDescription;

    @NotNull(message = "Rental price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rental price must be greater than 0")
    private BigDecimal rentalPrice;

    @NotNull(message = "Availability start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate availabilityStart;

    @NotNull(message = "Availability end date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate availabilityEnd;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getConditionDescription() {
        return conditionDescription;
    }

    public void setConditionDescription(String conditionDescription) {
        this.conditionDescription = conditionDescription;
    }

    public BigDecimal getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(BigDecimal rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public LocalDate getAvailabilityStart() {
        return availabilityStart;
    }

    public void setAvailabilityStart(LocalDate availabilityStart) {
        this.availabilityStart = availabilityStart;
    }

    public LocalDate getAvailabilityEnd() {
        return availabilityEnd;
    }

    public void setAvailabilityEnd(LocalDate availabilityEnd) {
        this.availabilityEnd = availabilityEnd;
    }
}
