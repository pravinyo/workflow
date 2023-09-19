package dev.pravin.workflow.lamf.request;

public record InitiateKycRequest(
        String customerId,
        String aadhaarId
) { }
