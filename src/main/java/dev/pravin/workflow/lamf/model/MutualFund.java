package dev.pravin.workflow.lamf.model;

import java.math.BigDecimal;

public record MutualFund(
        String name,
        BigDecimal quantity,
        BigDecimal unitPrice
) { }
