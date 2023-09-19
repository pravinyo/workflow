package dev.pravin.workflow.lamf.steps;


import dev.pravin.workflow.lamf.Constants;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StartStep implements WorkflowState<String> {
    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, String input, Persistence persistence, Communication communication) {
        persistence.setDataAttribute(Constants.DA_CURRENT_STEP, this.getClass().getSimpleName());
        return CommandRequest.empty;
    }

    @Override
    public StateDecision execute(Context context, String customerId, CommandResults commandResults, Persistence persistence, Communication communication) {
        // check whether there is any existing in progress application
        // navigate to that step
        // else start fresh
        return StateDecision.singleNextState(TermsAndConditionConsentStep.class);
    }
}
