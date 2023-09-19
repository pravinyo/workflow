package dev.pravin.workflow.kyc;

import dev.pravin.workflow.lamf.Constants;
import dev.pravin.workflow.lamf.LamfOrchestrationWorkflow;
import io.iworkflow.core.Client;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SaveAadhaarDetailsStep implements WorkflowState<String> {
    private final Client client;

    public SaveAadhaarDetailsStep(Client client) {
        this.client = client;
    }

    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public StateDecision execute(Context context, String aadhaarDetails, CommandResults commandResults, Persistence persistence, Communication communication) {
        log.info("save aadhaar details");
        var parentWorkflowId = persistence.getSearchAttributeText(Constants.SA_PARENT_WORKFLOW_ID);
        client.signalWorkflow(LamfOrchestrationWorkflow.class, parentWorkflowId, Constants.SC_SYSTEM_KYC_COMPLETED, "Success");
        return StateDecision.forceCompleteWorkflow("Aadhaar Kyc Finished");
    }
}
