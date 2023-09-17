package dev.pravin.workflow.lamf.request;

public record ValidateOtpRequest(
        String customerId,
        String otp
){}
