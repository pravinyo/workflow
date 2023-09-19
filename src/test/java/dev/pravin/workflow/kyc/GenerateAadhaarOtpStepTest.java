package dev.pravin.workflow.kyc;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GenerateAadhaarOtpStepTest {
    @Mock
    private Context context;
    @Mock
    private Persistence persistence;
    @Mock
    private Communication communication;
    @Mock
    private CommandResults commandResults;

    @Test
    void should_return_next_step_as_ValidateAadhaarOtpStep_when_otp_sent_successfully() {
        var generateAadhaarOtpStep = new GenerateAadhaarOtpStep();

        var actualStateDecision = generateAadhaarOtpStep.execute(
                context,
                "123412341234",
                commandResults,
                persistence,
                communication);

        var expectedStateDecision = StateDecision.singleNextState(ValidateAadhaarOtpStep.class, "REF-2345");
        assertEquals(expectedStateDecision, actualStateDecision);
    }

    @Test
    void should_set_search_attribute_when_otp_sent_successfully() {
        var generateAadhaarOtpStep = new GenerateAadhaarOtpStep();

        generateAadhaarOtpStep.execute(
                context,
                "123412341234",
                commandResults,
                persistence,
                communication);

        Mockito.verify(persistence, Mockito.times(1))
                .setSearchAttributeText("aadhaar_id", "123412341234");
    }
}