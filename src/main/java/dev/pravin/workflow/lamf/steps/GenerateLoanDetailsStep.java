package dev.pravin.workflow.lamf.steps;

import dev.pravin.workflow.lamf.model.ApplicationDetails;
import dev.pravin.workflow.lamf.Constants;
import dev.pravin.workflow.lamf.model.LoanDetails;
import dev.pravin.workflow.lamf.model.SelectedMutualFund;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

import java.math.BigDecimal;

public class GenerateLoanDetailsStep implements WorkflowState<Void> {
    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, Void input, Persistence persistence, Communication communication) {
        persistence.setDataAttribute(Constants.DA_CURRENT_STEP, this.getClass().getSimpleName());
        return CommandRequest.empty;
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        var applicationDetails = persistence.getDataAttribute(Constants.DA_APPLICATION_DETAILS, ApplicationDetails.class);
        var loanDetails = getLoanDetails(applicationDetails.getSelectedMutualFund());
        saveLoanDetails(persistence, loanDetails);
        return StateDecision.singleNextState(ReviewAndConfirmStep.class);
    }

    private LoanDetails getLoanDetails(SelectedMutualFund mutualFunds) {
        var principalAmount = mutualFunds.mutualFunds().stream()
                .map(mutualFund -> mutualFund.unitPrice().multiply(mutualFund.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var charges = BigDecimal.TEN;
        var tenure = 10;
        var rateOfInterest = 0.07;
        var interest = principalAmount
                .multiply(BigDecimal.valueOf(rateOfInterest))
                .multiply(BigDecimal.valueOf(tenure));
        var loanAmount = principalAmount.add(interest);

        return new LoanDetails(
                principalAmount,
                loanAmount,
                tenure,
                rateOfInterest,
                charges
        );
    }

    private void saveLoanDetails(Persistence persistence, LoanDetails loanDetails) {
        var applicationDetails = persistence.getDataAttribute(Constants.DA_APPLICATION_DETAILS, ApplicationDetails.class);
        applicationDetails.setLoanDetails(loanDetails);
        persistence.setDataAttribute(Constants.DA_APPLICATION_DETAILS, applicationDetails);
    }
}
