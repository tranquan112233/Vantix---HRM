package poly.edu.vantix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.response.DashboardResponse;
import poly.edu.vantix.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Thống kê tổng quan
    @GetMapping("/stats")
    public ResponseEntity<DashboardResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
