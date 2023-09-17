package dev.pravin.workflow.lamf.request;

public record GetStartedRequest(
        String customerId,
        Boolean isConsentProvided) { }
