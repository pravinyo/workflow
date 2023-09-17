package dev.pravin.workflow.lamf;

import dev.pravin.workflow.lamf.model.ApplicationDetails;
import dev.pravin.workflow.lamf.request.GetStartedRequest;
import dev.pravin.workflow.lamf.model.SelectedMutualFund;
import dev.pravin.workflow.lamf.request.ValidateOtpRequest;
import dev.pravin.workflow.lamf.steps.*;
import io.iworkflow.core.Client;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.SignalChannelDef;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.core.persistence.SearchAttributeDef;
import io.iworkflow.gen.models.SearchAttributeValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LamfOrchestrationWorkflow implements ObjectWorkflow {
    private final List<StateDef> stateDefs;
    @Autowired Client client;

    public LamfOrchestrationWorkflow() {
        this.stateDefs = List.of(
                StateDef.startingState(new StartStep()),
                StateDef.nonStartingState(new TermsAndConditionConsentStep()),
                StateDef.nonStartingState(new MutualFundPullGenerateOtpStep(client)),
                StateDef.nonStartingState(new MutualFundPullValidateOtpStep(client)),
                StateDef.nonStartingState(new MutualFundSchemesStep()),
                StateDef.nonStartingState(new GenerateLoanDetailsStep()),
                StateDef.nonStartingState(new ReviewAndConfirmStep())
        );
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return stateDefs;
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return List.of(
                DataAttributeDef.create(ApplicationDetails.class, Constants.DA_APPLICATION_DETAILS)
//                SearchAttributeDef.create(SearchAttributeValueType.TEXT),
        );
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return List.of(
                SignalChannelDef.create(GetStartedRequest.class, Constants.SC_USER_INPUT_CONSENT),
                SignalChannelDef.create(ValidateOtpRequest.class, Constants.SC_USER_INPUT_MF_PULL_OTP),
                SignalChannelDef.create(SelectedMutualFund.class, Constants.SC_USER_INPUT_MF_SCHEME_LIST)
        );
    }
}
