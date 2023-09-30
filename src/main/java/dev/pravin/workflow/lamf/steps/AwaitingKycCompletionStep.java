package dev.pravin.workflow.lamf.steps;

import dev.pravin.workflow.lamf.Constants;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.InternalChannelCommand;
import io.iworkflow.core.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AwaitingKycCompletionStep implements WorkflowState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, Void input, Persistence persistence, Communication communication) {
        persistence.setDataAttribute(Constants.DA_CURRENT_STEP, this.getClass().getSimpleName());
        return CommandRequest.forAllCommandCompleted(
                InternalChannelCommand.create(Constants.IC_SYSTEM_KYC_COMPLETED)
        );
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        var kycCompletionStatusOptional = commandResults.getAllInternalChannelCommandResult().get(0).getValue();

        if (kycCompletionStatusOptional.isPresent() && kycCompletionStatusOptional.get().equals(Constants.KYC_SUCCESS)) {
            log.info("Kyc completed");
            persistence.setDataAttribute(Constants.DA_CURRENT_STEP, "Done");
            return StateDecision.forceCompleteWorkflow("Done");
        } else {
            persistence.setDataAttribute(Constants.DA_CURRENT_STEP, "Failed");
            log.info("Kyc failed");
        }
        return StateDecision.singleNextState(ReviewAndConfirmStep.class);
    }
}
