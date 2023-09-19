package dev.pravin.workflow.kyc;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SaveAadhaarDetailsStep implements WorkflowState<String> {
    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public StateDecision execute(Context context, String aadhaarDetails, CommandResults commandResults, Persistence persistence, Communication communication) {
        log.info("save aadhaar details");
        return StateDecision.forceCompleteWorkflow("Aadhaar Kyc Finished");
    }
}
