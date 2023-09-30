package dev.pravin.workflow.kyc;

import dev.pravin.workflow.lamf.Constants;
import io.iworkflow.core.Client;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.SignalChannelDef;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.core.persistence.SearchAttributeDef;
import io.iworkflow.gen.models.SearchAttributeValueType;
import org.springframework.stereotype.Component;

import java.util.List;

import static dev.pravin.workflow.lamf.Constants.SC_AADHAAR_OTP_SIGNAL;

@Component
public class AadhaarKycWorkflow implements ObjectWorkflow {
    private final List<StateDef> stateDefs;

    public AadhaarKycWorkflow(Client client) {
        this.stateDefs = List.of(
                StateDef.startingState(new GenerateAadhaarOtpStep()),
                StateDef.nonStartingState(new ValidateAadhaarOtpStep(client)),
                StateDef.nonStartingState(new SaveAadhaarDetailsStep(client))
        );
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return stateDefs;
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return List.of(
                SearchAttributeDef.create(SearchAttributeValueType.TEXT, "customer_id"),
                SearchAttributeDef.create(SearchAttributeValueType.TEXT, "aadhaar_id"),
                SearchAttributeDef.create(SearchAttributeValueType.TEXT, "parentWorkflowId"),

                DataAttributeDef.create(Integer.class, Constants.OTP_ATTEMPT),
                DataAttributeDef.create(String.class, Constants.KYC_STATUS)
        );
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return List.of(
                SignalChannelDef.create(String.class, SC_AADHAAR_OTP_SIGNAL)
        );
    }
}
