package dev.pravin.workflow.lamf.model;

import java.math.BigDecimal;
import java.util.List;

public record SelectedMutualFund(
    List<MutualFund> mutualFunds,
    BigDecimal totalValue
) { }
