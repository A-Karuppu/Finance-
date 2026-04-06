package com.finance.dashboard.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MonthlyTrendItem {
    private int year;
    private int month;
    private String monthLabel;   // e.g. "Jan 2024"
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal net;
}
