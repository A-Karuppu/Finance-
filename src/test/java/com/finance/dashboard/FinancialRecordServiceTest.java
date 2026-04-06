package com.finance.dashboard;

import com.finance.dashboard.dto.CreateRecordRequest;
import com.finance.dashboard.dto.RecordFilterRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.dto.UpdateRecordRequest;
import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.service.FinancialRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceTest {

    @Mock private FinancialRecordRepository recordRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private FinancialRecordService recordService;

    private User adminUser;
    private FinancialRecord sampleRecord;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L).username("admin").email("admin@test.com")
                .role(Role.ADMIN).active(true).build();

        sampleRecord = FinancialRecord.builder()
                .id(1L)
                .amount(new BigDecimal("1500.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .transactionDate(LocalDate.now())
                .notes("Monthly salary")
                .createdBy(adminUser)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Mock security context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void createRecord_success() {
        CreateRecordRequest req = new CreateRecordRequest(
                new BigDecimal("1500.00"), TransactionType.INCOME,
                "Salary", LocalDate.now(), "Monthly salary");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(recordRepository.save(any())).thenReturn(sampleRecord);

        RecordResponse response = recordService.createRecord(req);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo("1500.00");
        assertThat(response.getCategory()).isEqualTo("Salary");
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
        verify(recordRepository).save(any(FinancialRecord.class));
    }

    @Test
    void getRecordById_found() {
        when(recordRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));

        RecordResponse response = recordService.getRecordById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCategory()).isEqualTo("Salary");
    }

    @Test
    void getRecordById_notFound_throwsException() {
        when(recordRepository.findByIdAndDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecordById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRecords_withFilters_returnsPaged() {
        Page<FinancialRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(recordRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        RecordFilterRequest filter = RecordFilterRequest.builder()
                .type(TransactionType.INCOME)
                .page(0).size(10)
                .sortBy("transactionDate").sortDir("desc")
                .build();

        var result = recordService.getRecords(filter);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateRecord_partialUpdate() {
        when(recordRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));
        when(recordRepository.save(any())).thenReturn(sampleRecord);

        UpdateRecordRequest req = new UpdateRecordRequest();
        req.setCategory("Freelance");

        RecordResponse response = recordService.updateRecord(1L, req);
        assertThat(response).isNotNull();
        verify(recordRepository).save(any());
    }

    @Test
    void deleteRecord_softDeletes() {
        when(recordRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));
        when(recordRepository.save(any())).thenReturn(sampleRecord);

        recordService.deleteRecord(1L);

        assertThat(sampleRecord.isDeleted()).isTrue();
        assertThat(sampleRecord.getDeletedAt()).isNotNull();
        verify(recordRepository).save(sampleRecord);
    }
}
