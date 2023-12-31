package dev.pravin.workflow.lamf.steps;

import dev.pravin.workflow.lamf.model.ApplicationDetails;
import dev.pravin.workflow.lamf.Constants;
import dev.pravin.workflow.lamf.request.ValidateOtpRequest;
import io.iworkflow.core.*;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.gen.models.RetryPolicy;
import io.iworkflow.gen.models.WorkflowResetType;
import io.iworkflow.gen.models.WorkflowStateOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class MutualFundPullValidateOtpStep implements WorkflowState<Void> {
    private final int maxOtpAttempt;
    private final Client client;

    public MutualFundPullValidateOtpStep(Client client, int maxOtpAttempt) {
        this.client = client;
        this.maxOtpAttempt = maxOtpAttempt;
    }

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, Void input, Persistence persistence, Communication communication) {
        persistence.setDataAttribute(Constants.DA_CURRENT_STEP, this.getClass().getSimpleName());
        return CommandRequest.forAnyCommandCompleted(
                SignalCommand.create(Constants.SC_USER_INPUT_MF_PULL_OTP)
        );
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        if (isRetryAttemptExhausted(persistence)) {
            var jumpToStep = "TermsAndConditionConsentStep";
            var reason = "RESETTING: OTP validate failed";
            resetToStep(context, jumpToStep, reason);
            return StateDecision.forceFailWorkflow(reason);
        }

        var requestObject = commandResults.getSignalValueByIndex(0);
        var validateOtpRequest = (ValidateOtpRequest) requestObject;
        var applicationDetails = persistence.getDataAttribute(Constants.DA_APPLICATION_DETAILS, ApplicationDetails.class);
        var isSuccess = validateOtp(applicationDetails.getMfPullOtpReferenceId(), validateOtpRequest.otp());
        if (isSuccess) {
            return StateDecision.singleNextState(MutualFundSchemesStep.class);
        }
        return StateDecision.singleNextState(MutualFundPullValidateOtpStep.class);
    }

    private void resetToStep(Context context, String jumpToStep, String reason) {
        var resetWorkflowTypeAndOptions = ResetWorkflowTypeAndOptions.builder()
                .resetType(WorkflowResetType.STATE_ID)
                .stateId(jumpToStep)
                .reason(reason)
                .skipSignalReapply(true)
                .build();
        client.resetWorkflow(context.getWorkflowId(), resetWorkflowTypeAndOptions);
    }

    private boolean validateOtp(String otpReferenceId, String otp) {
        log.info("calling external service for OTP validation with referenceId:{} and otp: {}", otpReferenceId, otp);
        return Objects.equals(otp, "1234");
    }

    private boolean isRetryAttemptExhausted(Persistence persistence) {
        var attemptLeft = persistence.getDataAttribute(Constants.DA_VALIDATE_OTP_ATTEMPT, Integer.class);
        if (Objects.isNull(attemptLeft)) {
            attemptLeft = maxOtpAttempt;
        }
        if (attemptLeft == 0) {
            return true;
        }
        persistence.setDataAttribute(Constants.DA_VALIDATE_OTP_ATTEMPT, attemptLeft - 1);
        return false;
    }

    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptions()
                .executeApiRetryPolicy(
                        new RetryPolicy()
                                .backoffCoefficient(2f)
                                .maximumAttempts(3)
                                .maximumAttemptsDurationSeconds(60)
                                .initialIntervalSeconds(3)
                                .maximumIntervalSeconds(60)
                );
    }
}
