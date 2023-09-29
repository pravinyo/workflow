package dev.pravin.workflow.lamf.response;

public record Response(
        String nextStep,
        Object data,
        Object errorDetails
) {
    public Response(Object data,
                    Object errorDetails) {
        this("", data, errorDetails);
    }
}