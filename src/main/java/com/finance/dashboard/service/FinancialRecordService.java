package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.util.FinancialRecordSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    // ---- Create ----
    @Transactional
    public RecordResponse createRecord(CreateRecordRequest request) {
        User currentUser = getCurrentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .transactionDate(request.getTransactionDate())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .deleted(false)
                .build();

        return toResponse(recordRepository.save(record));
    }

    // ---- Get by ID ----
    public RecordResponse getRecordById(Long id) {
        return toResponse(findActiveById(id));
    }

    // ---- List with filters + pagination ----
    public PagedResponse<RecordResponse> getRecords(RecordFilterRequest filter) {

        // Validate & sanitize sortBy to prevent injection
        String sortBy = isSafeField(filter.getSortBy()) ? filter.getSortBy() : "transactionDate";
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(),
                Sort.by(direction, sortBy));

        Specification<FinancialRecord> spec = FinancialRecordSpecification.withFilters(
                filter.getType(),
                filter.getCategory(),
                filter.getDateFrom(),
                filter.getDateTo(),
                filter.getAmountMin(),
                filter.getAmountMax()
        );

        Page<FinancialRecord> page = recordRepository.findAll(spec, pageable);

        return PagedResponse.<RecordResponse>builder()
                .content(page.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // ---- Update ----
    @Transactional
    public RecordResponse updateRecord(Long id, UpdateRecordRequest request) {
        FinancialRecord record = findActiveById(id);

        if (request.getAmount() != null)          record.setAmount(request.getAmount());
        if (request.getType() != null)            record.setType(request.getType());
        if (request.getCategory() != null)        record.setCategory(request.getCategory().trim());
        if (request.getTransactionDate() != null) record.setTransactionDate(request.getTransactionDate());
        if (request.getNotes() != null)           record.setNotes(request.getNotes());

        return toResponse(recordRepository.save(record));
    }

    // ---- Soft Delete ----
    @Transactional
    public void deleteRecord(Long id) {
        FinancialRecord record = findActiveById(id);
        record.setDeleted(true);
        record.setDeletedAt(LocalDateTime.now());
        recordRepository.save(record);
    }

    // ---- Helpers ----
    private FinancialRecord findActiveById(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record", id));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private boolean isSafeField(String field) {
        return List.of("transactionDate", "amount", "category", "type", "createdAt")
                .contains(field);
    }

    public RecordResponse toResponse(FinancialRecord r) {
        return RecordResponse.builder()
                .id(r.getId())
                .amount(r.getAmount())
                .type(r.getType())
                .category(r.getCategory())
                .transactionDate(r.getTransactionDate())
                .notes(r.getNotes())
                .createdBy(r.getCreatedBy().getUsername())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
