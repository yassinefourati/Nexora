package com.fourati.integration.credit;

import java.util.UUID;

public record CreditBureauReportRequest(UUID customerId, String nationalId) {
}
