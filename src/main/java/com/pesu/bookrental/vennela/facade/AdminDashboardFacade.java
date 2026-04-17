package com.pesu.bookrental.vennela.facade;

import com.pesu.bookrental.vennela.service.HomeService;
import com.pesu.bookrental.vennela.service.ReportService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AdminDashboardFacade {

    private final HomeService homeService;
    private final ReportService reportService;

    public AdminDashboardFacade(HomeService homeService, ReportService reportService) {
        this.homeService = homeService;
        this.reportService = reportService;
    }

    public Map<String, Long> getPlatformStats() {
        return homeService.getPlatformStats();
    }

    public Map<String, String> getAnalytics() {
        return homeService.getAdminAnalytics();
    }

    public Map<String, Long> getReportStats() {
        return reportService.getReportStats();
    }

    public Object getReports() {
        return reportService.findAllReports();
    }
}
