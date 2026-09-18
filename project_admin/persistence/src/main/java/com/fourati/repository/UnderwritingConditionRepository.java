package com.fourati.repository;

import com.fourati.domain.UnderwritingCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnderwritingConditionRepository extends JpaRepository<UnderwritingCondition, UUID> {

    List<UnderwritingCondition> findByUnderwritingCaseId(UUID underwritingCaseId);
}
