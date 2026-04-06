package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final FinancialRecordService recordService;

    public DashboardSummary getSummary() {
        BigDecimal totalIncome   = recordRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);
        long totalRecords        = recordRepository.countByDeletedFalse();

        // Category totals (all types combined)
        Map<String, BigDecimal> categoryTotals = buildCategoryMap(
                recordRepository.sumGroupedByCategory());

        // Income by category
        Map<String, BigDecimal> incomeCategoryTotals = buildCategoryMap(
                recordRepository.sumGroupedByCategoryAndType(TransactionType.INCOME));

        // Expense by category
        Map<String, BigDecimal> expenseCategoryTotals = buildCategoryMap(
                recordRepository.sumGroupedByCategoryAndType(TransactionType.EXPENSE));

        // Monthly trend — last 6 months
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        List<MonthlyTrendItem> monthlyTrend = buildMonthlyTrend(
                recordRepository.monthlyTrend(sixMonthsAgo));

        // Recent activity — last 10 records
        List<RecordResponse> recentActivity = recordRepository
                .findRecentActivity(PageRequest.of(0, 10))
                .stream()
                .map(recordService::toResponse)
                .collect(Collectors.toList());

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalRecords(totalRecords)
                .categoryTotals(categoryTotals)
                .incomeCategoryTotals(incomeCategoryTotals)
                .expenseCategoryTotals(expenseCategoryTotals)
                .monthlyTrend(monthlyTrend)
                .recentActivity(recentActivity)
                .build();
    }

    // ---- Helpers ----

    private Map<String, BigDecimal> buildCategoryMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            map.put(category, total);
        }
        return map;
    }

    /**
     * Converts raw [year, month, type, total] rows into MonthlyTrendItem list.
     * Groups income and expense for the same year/month into one object.
     */
    private List<MonthlyTrendItem> buildMonthlyTrend(List<Object[]> rows) {
        // Key: "year-month"
        Map<String, MonthlyTrendItem> trendMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            int year  = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            TransactionType type = TransactionType.valueOf((String) row[2]);
            BigDecimal total = (BigDecimal) row[3];

            String key = year + "-" + month;
            trendMap.computeIfAbsent(key, k -> {
                String label = Month.of(month)
                        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year;
                return MonthlyTrendItem.builder()
                        .year(year).month(month).monthLabel(label)
                        .income(BigDecimal.ZERO).expenses(BigDecimal.ZERO)
                        .build();
            });

            MonthlyTrendItem item = trendMap.get(key);
            if (type == TransactionType.INCOME) {
                item.setIncome(total);
            } else {
                item.setExpenses(total);
            }
            item.setNet(item.getIncome().subtract(item.getExpenses()));
        }

        return new ArrayList<>(trendMap.values());
    }
}
