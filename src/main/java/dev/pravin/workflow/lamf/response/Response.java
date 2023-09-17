package dev.pravin.workflow.lamf.response;

public record Response(
        String nextStep,
        Object data,
        Object errorDetails
) { }