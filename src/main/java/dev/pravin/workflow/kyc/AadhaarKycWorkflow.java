package dev.pravin.workflow.kyc;

import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.SignalChannelDef;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.core.persistence.SearchAttributeDef;
import io.iworkflow.gen.models.SearchAttributeValueType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AadhaarKycWorkflow implements ObjectWorkflow {
    private final List<StateDef> stateDefs;

    public AadhaarKycWorkflow() {
        this.stateDefs = List.of(
                StateDef.startingState(new GenerateAadhaarOtpStep()),
                StateDef.nonStartingState(new ValidateAadhaarOtpStep()),
                StateDef.nonStartingState(new SaveAadhaarDetailsStep())
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
                SearchAttributeDef.create(SearchAttributeValueType.TEXT, "aadhaar_id")
        );
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return List.of(SignalChannelDef.create(String.class, "AadhaarOtpSignal"));
    }
}
