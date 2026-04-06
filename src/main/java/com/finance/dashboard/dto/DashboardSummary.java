package com.finance.dashboard.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private long totalRecords;
    private Map<String, BigDecimal> categoryTotals;        // All categories
    private Map<String, BigDecimal> incomeCategoryTotals;  // Income by category
    private Map<String, BigDecimal> expenseCategoryTotals; // Expense by category
    private List<MonthlyTrendItem> monthlyTrend;           // Last 6 months
    private List<RecordResponse> recentActivity;           // Last 10 records
}
