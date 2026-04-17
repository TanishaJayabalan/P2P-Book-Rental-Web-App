package com.pesu.bookrental.vennela.command;

import com.pesu.bookrental.domain.enums.ReportStatus;
import com.pesu.bookrental.vennela.service.ReportService;
import org.springframework.stereotype.Component;

@Component
public class MarkReportUnderReviewCommand implements ReportCommand {

    private final ReportService reportService;

    public MarkReportUnderReviewCommand(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void execute(Long reportId) {
        reportService.updateStatus(reportId, ReportStatus.UNDER_REVIEW);
    }
}
