package dev.pravin.workflow.shop.scheduler.steps;

import dev.pravin.workflow.shop.scheduler.model.SendEmailCommunication;
import dev.pravin.workflow.shop.scheduler.model.SendEmailCommunicationWrapper;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

public class SendEmailStep implements WorkflowState<SendEmailCommunicationWrapper> {

    @Override
    public Class<SendEmailCommunicationWrapper> getInputType() {
        return SendEmailCommunicationWrapper.class;
    }

    @Override
    public StateDecision execute(Context context, SendEmailCommunicationWrapper communicationWrapper, CommandResults commandResults, Persistence persistence, Communication communication) {
        System.out.println("Started with sending email");

        communicationWrapper
                .getEmailCommunications()
                .forEach(this::pushEventToKafka);
        System.out.println("Completed with sending email");
        return StateDecision.forceCompleteWorkflow("Done");
    }

    private void pushEventToKafka(SendEmailCommunication emailCommunication) {
        System.out.println("Email sent for communication:\n " + emailCommunication);
    }
}
