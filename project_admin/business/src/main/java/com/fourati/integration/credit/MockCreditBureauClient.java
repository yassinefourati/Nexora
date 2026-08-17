package com.fourati.integration.credit;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Deterministic mock credit bureau for local development and tests — no
 * real bureau connectivity is required to run the application. Derives a
 * stable score from the customer id so the same customer always gets the
 * same mock result.
 */
@Component
public class MockCreditBureauClient implements CreditBureauClient {

    @Override
    public CreditBureauReportResponse getCreditReport(CreditBureauReportRequest request) {
        int score = 580 + Math.floorMod(request.customerId().hashCode(), 271); // 580-850
        return new CreditBureauReportResponse(
                "MockBureau",
                "MOCK-" + UUID.randomUUID(),
                score,
                score,
                "FICO-MOCK",
                BigDecimal.valueOf(0.35)
        );
    }
}
