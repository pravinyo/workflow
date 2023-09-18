package dev.pravin.workflow.lamf.response;

public record Response(
        Object data,
        Object errorDetails
) { }