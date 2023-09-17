package dev.pravin.workflow.lamf.request;

import dev.pravin.workflow.lamf.model.SelectedMutualFund;

public record SelectedMutualFundRequest(
    SelectedMutualFund selectedMutualFund,
    String customerId
) { }