package com.pesu.bookrental.vennela.service;

import com.pesu.bookrental.vennela.dto.ReportForm;
import com.pesu.bookrental.domain.enums.ReportStatus;
import com.pesu.bookrental.domain.model.Report;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.factory.BookRentalFactory;
import com.pesu.bookrental.repository.ReportRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final BookRentalFactory bookRentalFactory;

    public ReportService(ReportRepository reportRepository,
                         BookRentalFactory bookRentalFactory) {
        this.reportRepository = reportRepository;
        this.bookRentalFactory = bookRentalFactory;
    }

    @Transactional
    public Report createReport(ReportForm form, User reportedBy, User targetUser) {
        Report report = bookRentalFactory.createReport(
                form.getReportType().trim(),
                form.getDescription().trim(),
                reportedBy,
                targetUser
        );
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<Report> findAllReports() {
        return reportRepository.findAllDetailed();
    }

    @Transactional
    public void updateStatus(Long reportId, ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found."));
        report.setStatus(status);
        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getReportStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("Open Reports", reportRepository.countByStatus(ReportStatus.OPEN));
        stats.put("Under Review", reportRepository.countByStatus(ReportStatus.UNDER_REVIEW));
        stats.put("Resolved", reportRepository.countByStatus(ReportStatus.RESOLVED));
        return stats;
    }
}
