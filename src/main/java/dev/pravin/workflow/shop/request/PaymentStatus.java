package dev.pravin.workflow.shop.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatus{
    private String workflowId;
    private String transactionId;
    private Boolean isSuccessful;
}
