package com.pesu.bookrental.vennela.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReportForm {

    @NotBlank(message = "Please select a report type.")
    private String reportType;

    @NotBlank(message = "Please describe the issue.")
    @Size(max = 1000, message = "Description must be at most 1000 characters.")
    private String description;

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
