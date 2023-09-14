package dev.pravin.workflow.shop.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendEmailCommunicationWrapper {
    private List<SendEmailCommunication> emailCommunications;
}
