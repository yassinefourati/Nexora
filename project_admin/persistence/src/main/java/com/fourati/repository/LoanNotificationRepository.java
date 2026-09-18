package com.fourati.repository;

import com.fourati.domain.LoanNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanNotificationRepository extends JpaRepository<LoanNotification, UUID> {

    List<LoanNotification> findByLoanApplicationIdOrderByCreatedAtDesc(UUID loanApplicationId);
}
