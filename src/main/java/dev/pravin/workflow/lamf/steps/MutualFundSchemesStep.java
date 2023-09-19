package dev.pravin.workflow.lamf.steps;

import dev.pravin.workflow.lamf.model.ApplicationDetails;
import dev.pravin.workflow.lamf.Constants;
import dev.pravin.workflow.lamf.model.SelectedMutualFund;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class MutualFundSchemesStep implements WorkflowState<Void> {
    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, Void input, Persistence persistence, Communication communication) {
        persistence.setDataAttribute(Constants.DA_CURRENT_STEP, this.getClass().getSimpleName());
        return CommandRequest.forAnyCommandCompleted(
                SignalCommand.create(Constants.SC_USER_INPUT_MF_SCHEME_LIST)
        );
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        var requestObject = commandResults.getSignalValueByIndex(0);
        var selectedMutualFund = (SelectedMutualFund)requestObject;
        if(isValidRequest(selectedMutualFund)) {
            saveMutualFundDetails(persistence, selectedMutualFund);
            return StateDecision.singleNextState(GenerateLoanDetailsStep.class);
        }
        return StateDecision.singleNextState(MutualFundSchemesStep.class);
    }

    private void saveMutualFundDetails(Persistence persistence, SelectedMutualFund selectedMutualFund) {
        var applicationDetails = persistence.getDataAttribute(Constants.DA_APPLICATION_DETAILS, ApplicationDetails.class);
        applicationDetails.setSelectedMutualFund(selectedMutualFund);
        persistence.setDataAttribute(Constants.DA_APPLICATION_DETAILS, applicationDetails);
    }

    private boolean isValidRequest(SelectedMutualFund selectedMutualFund) {
        var total = selectedMutualFund.mutualFunds().stream()
                .map(mutualFund -> mutualFund.unitPrice().multiply(mutualFund.quantity()))
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        if (total.equals(selectedMutualFund.totalValue())) {
            return true;
        }

        log.error("Mutual fund value mismatch");
        return false;
    }
}
