package dev.pravin.workflow.lamf.steps;

import com.sun.jdi.InternalException;
import dev.pravin.workflow.lamf.model.ApplicationDetails;
import dev.pravin.workflow.lamf.Constants;
import io.iworkflow.core.*;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.gen.models.RetryPolicy;
import io.iworkflow.gen.models.WorkflowResetType;
import io.iworkflow.gen.models.WorkflowStateOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class MutualFundPullGenerateOtpStep implements WorkflowState<String> {
    @Value("${mf-pull.generate-otp-attempt}")
    private int maxOtpAttempt;

    private final Client client;

    public MutualFundPullGenerateOtpStep(Client client) {
        this.client = client;
    }

    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public StateDecision execute(Context context, String customerId, CommandResults commandResults, Persistence persistence, Communication communication) {
        validateRequest(customerId);
        if (isRetryAttemptExhausted(context)) {
            var jumpToStep = "TermsAndConditionConsentStep";
            var reason = "RESETTING: OTP generate failed";
            resetToStep(context, jumpToStep, reason);
            return StateDecision.forceFailWorkflow(reason);
        }

        var mobileNumber = "1231231231";
        var email = "pravin@pravin.dev";
        triggerOtp(persistence, mobileNumber, email);
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

    private void triggerOtp(Persistence persistence, String mobileNumber, String email) {
        var requestId = "REQ001";
        saveRequestId(persistence, requestId);
        var otpMessage = """
                Dear Customer,
                Your OTP is 1234 for LAMF application.
                """;
        System.out.println("""
                ${message}
                                
                send on ${mobile} and ${email}
                """
                .replace("${message}", otpMessage)
                .replace("${mobile}", mobileNumber)
                .replace("${email}", email)
        );
//        throw new InternalException("External System for OTP generation is down");
    }

    private void saveRequestId(Persistence persistence, String requestId) {
        var applicationDetails = persistence.getDataAttribute(Constants.DA_APPLICATION_DETAILS, ApplicationDetails.class);
        applicationDetails.setMfPullOtpReferenceId(requestId);
        persistence.setDataAttribute(Constants.DA_APPLICATION_DETAILS, applicationDetails);
    }

    private boolean isRetryAttemptExhausted(Context context) {
        if (context.getAttempt().isPresent()) {
            return context.getAttempt().get() == maxOtpAttempt;
        }
        return false;
    }

    private void validateRequest(String customerId) {
        if (customerId.isBlank()) {
            throw new IllegalArgumentException("Customer Id cannot be empty");
        }
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
