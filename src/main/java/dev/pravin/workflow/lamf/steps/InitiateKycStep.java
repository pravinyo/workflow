package dev.pravin.workflow.lamf.steps;

import dev.pravin.workflow.kyc.AadhaarKycWorkflow;
import dev.pravin.workflow.lamf.Constants;
import dev.pravin.workflow.lamf.request.InitiateKycRequest;
import io.iworkflow.core.*;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.gen.models.WorkflowResetType;
import io.iworkflow.gen.models.WorkflowStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class InitiateKycStep implements WorkflowState<Void> {
    private final Client client;

    public InitiateKycStep(Client client) {
        this.client = client;
    }

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, Void input, Persistence persistence, Communication communication) {
        persistence.setDataAttribute(Constants.DA_CURRENT_STEP, this.getClass().getSimpleName());
        return CommandRequest.forAllCommandCompleted(
                SignalCommand.create(Constants.SC_USER_INPUT_KYC)
        );
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        var initiateKycRequest =  (InitiateKycRequest) commandResults.getSignalValueByIndex(0);
        var workflowId = getWorkflowIdForAadhaar(initiateKycRequest.customerId());
        try {
            var response = client.describeWorkflow(workflowId);
            if (response.getWorkflowStatus().equals(WorkflowStatus.RUNNING)) {
                resetWorkflow(context);
                initiateKycWorkflow(initiateKycRequest, workflowId, context.getWorkflowId());
                return StateDecision.singleNextState(AwaitingKycCompletionStep.class);

            } else if (response.getWorkflowStatus().equals(WorkflowStatus.COMPLETED)) {
                log.info("Kyc already done");
                persistence.setDataAttribute(Constants.DA_CURRENT_STEP, "Done");
                return StateDecision.forceCompleteWorkflow("Done");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        initiateKycWorkflow(initiateKycRequest, workflowId, context.getWorkflowId());
        return StateDecision.singleNextState(AwaitingKycCompletionStep.class);
    }

    private void resetWorkflow(Context context) {
        var resetWorkflowTypeAndOptions = ResetWorkflowTypeAndOptions.builder()
                .resetType(WorkflowResetType.BEGINNING)
                .reason("resetting kyc")
                .skipSignalReapply(true)
                .build();
        client.resetWorkflow(context.getWorkflowId(), resetWorkflowTypeAndOptions);
    }
    private void initiateKycWorkflow(InitiateKycRequest initiateKycRequest, String childWorkflowId, String workflowId) {
        var options = ImmutableWorkflowOptions.builder()
                .initialSearchAttribute(Map.of(
                        Constants.SA_CUSTOMER_ID, initiateKycRequest.customerId(),
                        Constants.SA_PARENT_WORKFLOW_ID, workflowId))
                .build();
        client.startWorkflow(AadhaarKycWorkflow.class, childWorkflowId, 3600, initiateKycRequest.aadhaarId(), options);
    }

    private String getWorkflowIdForAadhaar(String customerId) {
        return "WF-LAMF-KYC-"+customerId;
    }
}
