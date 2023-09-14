package dev.pravin.workflow.shop.scheduler;

import dev.pravin.workflow.shop.scheduler.steps.FetchAllStaleOrdersStep;
import dev.pravin.workflow.shop.scheduler.steps.GenerateEmailStep;
import dev.pravin.workflow.shop.scheduler.steps.SendEmailStep;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaleOrderNotificationWorkflow implements ObjectWorkflow {
    private final List<StateDef> stateDefs;

    public StaleOrderNotificationWorkflow() {
        this.stateDefs = List.of(
                StateDef.startingState(new FetchAllStaleOrdersStep()),
                StateDef.nonStartingState(new GenerateEmailStep()),
                StateDef.nonStartingState(new SendEmailStep())
        );
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return stateDefs;
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return List.of();
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return List.of();
    }
}
