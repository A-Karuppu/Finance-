package com.finance.dashboard.controller;

import com.finance.dashboard.dto.ApiResponse;
import com.finance.dashboard.dto.DashboardSummary;
import com.finance.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/summary
     * Returns complete dashboard summary:
     *   - Total income, expenses, net balance
     *   - Record count
     *   - Category-wise totals (all, income, expense)
     *   - Monthly trend (last 6 months)
     *   - Recent activity (last 10 records)
     *
     * Accessible by: VIEWER, ANALYST, ADMIN
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardSummary>> getSummary() {
        DashboardSummary summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
