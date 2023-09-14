package dev.pravin.workflow.shop.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendEmailCommunication {
    private String senderEmailId;
    private String receiverEmailId;
    private String subject;
    private String body;
}
