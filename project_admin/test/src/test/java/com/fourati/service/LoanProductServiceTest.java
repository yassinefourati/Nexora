package com.fourati.service;

import com.fourati.domain.LoanProduct;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.response.LoanProductResponse;
import com.fourati.mapper.LoanProductMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanProductServiceTest {

    @Mock
    private LoanProductRepository loanProductRepository;

    @Mock
    private LoanProductMapper loanProductMapper;

    @InjectMocks
    private LoanProductService loanProductService;

    private CreateLoanProductRequest newRequest() {
        return new CreateLoanProductRequest(
                "PL-001",
                "Personal Loan",
                "personal",
                "active",
                "USD",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(50000),
                6,
                84,
                "Standard personal loan"
        );
    }

    @Test
    void create_savesLoanProduct() {
        CreateLoanProductRequest request = newRequest();
        LoanProduct entity = new LoanProduct();

        when(loanProductRepository.existsByCode(request.code())).thenReturn(false);
        when(loanProductMapper.toEntity(request)).thenReturn(entity);
        when(loanProductRepository.save(any(LoanProduct.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanProductMapper.toResponse(any(LoanProduct.class))).thenReturn(
                new LoanProductResponse(UUID.randomUUID(), request.code(), request.name(), request.productType(),
                        "active", "USD", request.minAmount(), request.maxAmount(),
                        request.minTermMonths(), request.maxTermMonths(), request.description(), null, null));

        LoanProductResponse response = loanProductService.create(request);

        assertThat(response.code()).isEqualTo(request.code());
        verify(loanProductRepository).save(entity);
    }

    @Test
    void create_throwsConflict_whenCodeAlreadyExists() {
        CreateLoanProductRequest request = newRequest();
        when(loanProductRepository.existsByCode(request.code())).thenReturn(true);

        assertThatThrownBy(() -> loanProductService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(loanProductRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(loanProductRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanProductService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_setsDeletedAt() {
        UUID id = UUID.randomUUID();
        LoanProduct entity = new LoanProduct();
        when(loanProductRepository.findById(id)).thenReturn(Optional.of(entity));
        when(loanProductRepository.save(any(LoanProduct.class))).thenAnswer(inv -> inv.getArgument(0));

        loanProductService.delete(id);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(loanProductRepository).save(entity);
    }
}
