package com.finance.dashboard.repository;

import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>,
                JpaSpecificationExecutor<FinancialRecord> {

    // Soft-delete aware fetch
    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    Page<FinancialRecord> findByDeletedFalse(Pageable pageable);

    // ---- Dashboard aggregates ----

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.type = :type AND r.deleted = false")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("SELECT r.category, COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.deleted = false GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumGroupedByCategory();

    @Query("SELECT r.category, COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.type = :type AND r.deleted = false " +
           "GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumGroupedByCategoryAndType(@Param("type") TransactionType type);

    // Monthly trend: returns [year, month, type, total]
    @Query("SELECT YEAR(r.transactionDate), MONTH(r.transactionDate), r.type, " +
           "COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.deleted = false " +
           "AND r.transactionDate >= :from " +
           "GROUP BY YEAR(r.transactionDate), MONTH(r.transactionDate), r.type " +
           "ORDER BY YEAR(r.transactionDate), MONTH(r.transactionDate)")
    List<Object[]> monthlyTrend(@Param("from") LocalDate from);

    // Recent activity
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false " +
           "ORDER BY r.transactionDate DESC")
    List<FinancialRecord> findRecentActivity(Pageable pageable);

    // Count
    long countByDeletedFalse();
}
