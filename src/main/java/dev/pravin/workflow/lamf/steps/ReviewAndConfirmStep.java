package dev.pravin.workflow.lamf.steps;

import dev.pravin.workflow.lamf.model.ApplicationDetails;
import dev.pravin.workflow.lamf.Constants;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReviewAndConfirmStep implements WorkflowState<Void> {
    @Override
    public Class<Void> getInputType() {
        return null;
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        updateLoanDetails(persistence);
        performSoftSanction();
        return StateDecision.forceCompleteWorkflow("Done");
    }

    private void performSoftSanction() {
        // make API call to external system to save the risk decision
    }

    private void updateLoanDetails(Persistence persistence) {
        // update loan details in DB
        var applicationDetails = persistence.getDataAttribute(Constants.DA_APPLICATION_DETAILS, ApplicationDetails.class);
        log.info("Loan details is : {}", applicationDetails.getLoanDetails());
    }
}
