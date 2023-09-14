package dev.pravin.workflow.shop.scheduler.steps;

import dev.pravin.workflow.shop.model.Item;
import dev.pravin.workflow.shop.model.Order;
import dev.pravin.workflow.shop.scheduler.model.GenerateEmail;
import dev.pravin.workflow.shop.scheduler.model.SendEmailCommunication;
import dev.pravin.workflow.shop.scheduler.model.SendEmailCommunicationWrapper;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.gen.models.RetryPolicy;
import io.iworkflow.gen.models.WorkflowStateOptions;

import java.util.List;
import java.util.stream.Collectors;

public class GenerateEmailStep implements WorkflowState<GenerateEmail> {

    @Override
    public Class<GenerateEmail> getInputType() {
        return GenerateEmail.class;
    }

    @Override
    public StateDecision execute(Context context, GenerateEmail generateEmail, CommandResults commandResults, Persistence persistence, Communication communication) {
        System.out.println("started with Generating email");
        System.out.println(generateEmail);
        SendEmailCommunicationWrapper communications = getEmailCommunications(generateEmail);
        System.out.println("Completed Generating email");
        return StateDecision.singleNextState(SendEmailStep.class, communications);
    }

    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptions()
                .executeApiRetryPolicy(
                        new RetryPolicy()
                                .backoffCoefficient(2f)
                                .maximumAttempts(3)
                                .maximumAttemptsDurationSeconds(3600)
                                .initialIntervalSeconds(3)
                                .maximumIntervalSeconds(60)
                );
    }

    private SendEmailCommunicationWrapper getEmailCommunications(GenerateEmail generateEmail) {
        var emailCommunications = generateEmail.getOrders()
                .stream()
                .map(this::generateEmail)
                .collect(Collectors.toList());
        return SendEmailCommunicationWrapper.builder()
                .emailCommunications(emailCommunications).build();
    }

    private SendEmailCommunication generateEmail(Order order) {
        var subject = "Order is yet to be placed!";
        var body = "Dear " + order.getCustomerId() + "," +
                "\n Your order for below items is in cart " + getItems(order) +
                "\n click below to continue with the order placing.";

        return SendEmailCommunication.builder()
                .body(body)
                .subject(subject)
                .senderEmailId("no-reply@pravin.dev")
                .receiverEmailId("dummy@pravin.dev")
                .build();
    }

    private List<String> getItems(Order order) {
        return order.getItems().stream().map(Item::getName)
        .collect(Collectors.toList());
    }
}
