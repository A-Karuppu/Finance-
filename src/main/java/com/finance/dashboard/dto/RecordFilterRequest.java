package com.finance.dashboard.dto;

import com.finance.dashboard.enums.TransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecordFilterRequest {
    private TransactionType type;
    private String category;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal amountMin;
    private BigDecimal amountMax;

    // Pagination & sorting
    private int page = 0;
    private int size = 20;
    private String sortBy = "transactionDate";
    private String sortDir = "desc";
}
