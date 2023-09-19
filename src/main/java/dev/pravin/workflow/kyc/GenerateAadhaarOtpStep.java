package dev.pravin.workflow.kyc;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateAadhaarOtpStep implements WorkflowState<String> {
    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public StateDecision execute(Context context, String aadhaarNumber, CommandResults commandResults, Persistence persistence, Communication communication) {
        persistence.setSearchAttributeText("aadhaar_id", aadhaarNumber);
        var referenceId = sendAadhaarOtp(aadhaarNumber);
        return StateDecision.singleNextState(ValidateAadhaarOtpStep.class, referenceId);
    }

    private String sendAadhaarOtp(String aadhaarNumber) {
        log.info("calling aadhaar API to send OTP for {}", aadhaarNumber);
        return "REF-2345";
    }
}
