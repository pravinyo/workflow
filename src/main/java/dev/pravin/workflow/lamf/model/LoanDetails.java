package dev.pravin.workflow.lamf.model;

import java.math.BigDecimal;

public record LoanDetails(
        BigDecimal principalAmount,
        BigDecimal loanAmount,
        int tenure,
        double rateOfInterest,
        BigDecimal totalProcessingFees) { }
