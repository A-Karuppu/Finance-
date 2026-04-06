package com.finance.dashboard.controller;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    /**
     * POST /api/records
     * Create a new financial record. ADMIN only.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            @Valid @RequestBody CreateRecordRequest request) {
        RecordResponse record = recordService.createRecord(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Record created successfully", record));
    }

    /**
     * GET /api/records/{id}
     * Fetch a single record. ANALYST or ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<RecordResponse>> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(recordService.getRecordById(id)));
    }

    /**
     * GET /api/records
     * List records with optional filters and pagination.
     * ANALYST or ADMIN.
     *
     * Query Parameters:
     *   type        - INCOME | EXPENSE
     *   category    - partial match, case-insensitive
     *   dateFrom    - yyyy-MM-dd
     *   dateTo      - yyyy-MM-dd
     *   amountMin   - e.g. 100.00
     *   amountMax   - e.g. 5000.00
     *   page        - default 0
     *   size        - default 20
     *   sortBy      - transactionDate | amount | category | createdAt  (default: transactionDate)
     *   sortDir     - asc | desc  (default: desc)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<RecordResponse>>> getRecords(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal amountMin,
            @RequestParam(required = false) BigDecimal amountMax,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        RecordFilterRequest filter = RecordFilterRequest.builder()
                .type(type)
                .category(category)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .amountMin(amountMin)
                .amountMax(amountMax)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PagedResponse<RecordResponse> result = recordService.getRecords(filter);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * PUT /api/records/{id}
     * Update an existing record. ADMIN only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecordRequest request) {
        RecordResponse updated = recordService.updateRecord(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Record updated successfully", updated));
    }

    /**
     * DELETE /api/records/{id}
     * Soft-delete a record (marks deleted=true). ADMIN only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.ok("Record deleted", null));
    }
}
