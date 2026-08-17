package com.fourati.integration.credit;

/**
 * Abstraction over an external credit bureau. Never call an external
 * financial provider directly from a service — always go through this
 * interface, so local development and tests don't require real bureau
 * connectivity.
 */
public interface CreditBureauClient {

    CreditBureauReportResponse getCreditReport(CreditBureauReportRequest request);
}
