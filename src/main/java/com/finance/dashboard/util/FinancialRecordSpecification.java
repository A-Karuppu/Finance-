package com.finance.dashboard.util;

import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic JPA Specification from optional filter parameters.
 * Only non-null parameters are added as predicates.
 */
public class FinancialRecordSpecification {

    private FinancialRecordSpecification() {}

    public static Specification<FinancialRecord> withFilters(
            TransactionType type,
            String category,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal amountMin,
            BigDecimal amountMax
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.isFalse(root.get("deleted")));

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("category")),
                        "%" + category.toLowerCase() + "%"
                ));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), dateTo));
            }
            if (amountMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), amountMin));
            }
            if (amountMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), amountMax));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
