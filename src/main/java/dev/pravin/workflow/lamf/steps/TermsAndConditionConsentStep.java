package dev.pravin.workflow.lamf.steps;

import dev.pravin.workflow.lamf.model.ApplicationDetails;
import dev.pravin.workflow.lamf.Constants;
import dev.pravin.workflow.lamf.request.GetStartedRequest;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

import static dev.pravin.workflow.lamf.Constants.DA_APPLICATION_DETAILS;

@Slf4j
public class TermsAndConditionConsentStep implements WorkflowState<Void> {
    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, Void input, Persistence persistence, Communication communication) {
        return CommandRequest.forAnyCommandCompleted(
                SignalCommand.create(Constants.SC_USER_INPUT_CONSENT)
        );
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        var requestObject = commandResults.getSignalValueByIndex(0);
        var getStartedRequest = (GetStartedRequest) requestObject;

        if (!getStartedRequest.isConsentProvided()) {
            return StateDecision.singleNextState(TermsAndConditionConsentStep.class);
        }
        updateConsent(persistence, getStartedRequest);
        return StateDecision.singleNextState(MutualFundPullGenerateOtpStep.class, getStartedRequest.customerId());
    }

    private void updateConsent(Persistence persistence, GetStartedRequest getStartedRequest) {
        var applicationDetails = ApplicationDetails.builder()
                .isConsentProvided(getStartedRequest.isConsentProvided())
                .build();
        persistence.setDataAttribute(DA_APPLICATION_DETAILS, applicationDetails);

        log.info("Consent updated for customer Id: {} with value:{}",
                getStartedRequest.customerId(), getStartedRequest.isConsentProvided());
    }
}
