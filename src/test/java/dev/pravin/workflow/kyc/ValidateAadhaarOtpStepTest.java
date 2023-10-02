package dev.pravin.workflow.kyc;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.Persistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidateAadhaarOtpStepTest {

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
        var validateAadhaarOtpStep = new ValidateAadhaarOtpStep(Mockito.mock());

        var actualCommandsToWait = validateAadhaarOtpStep.waitUntil(
                context,
                "REF-2345",
                persistence,
                communication);

        var expectedStateDecision = CommandRequest.forAllCommandCompleted(
                SignalCommand.create("sc_aadhaar_otp_signal")
        );
        assertEquals(expectedStateDecision, actualCommandsToWait);
    }

    @Test
    void should_return_State_decision_as_SaveAadhaarDetailsStep_for_valid_otp() {
        var validateAadhaarOtpStep = new ValidateAadhaarOtpStep(Mockito.mock());
        Mockito.when(commandResults.getSignalValueByIndex(Mockito.anyInt()))
                .thenReturn("1234");

        var actualStateDecision = validateAadhaarOtpStep.execute(
                context,
                "REF-2345",
                commandResults,
                persistence,
                communication);

        var expectedStateDecision = StateDecision.singleNextState(SaveAadhaarDetailsStep.class, getDetails());
        assertEquals(expectedStateDecision, actualStateDecision);
    }

    @Test
    void should_return_State_decision_as_ValidateAadhaarOtpStep_for_invalid_otp() {
        var validateAadhaarOtpStep = new ValidateAadhaarOtpStep(Mockito.mock());
        Mockito.when(commandResults.getSignalValueByIndex(Mockito.anyInt()))
                .thenReturn("1233");

        var actualStateDecision = validateAadhaarOtpStep.execute(
                context,
                "REF-2345",
                commandResults,
                persistence,
                communication);

        var expectedStateDecision = StateDecision.singleNextState(ValidateAadhaarOtpStep.class, "REF-2345");
        assertEquals(expectedStateDecision, actualStateDecision);
    }

    private Object getDetails() {
        return """
                {
                    "_id": "6509549c6c3a1d2c757a0a25",
                    "index": 0,
                    "guid": "29d7fa96-4449-42c2-aa3e-de92fa73c4c0",
                    "isActive": true,
                    "balance": "$1,226.50",
                    "picture": "http://placehold.it/32x32",
                    "age": 26,
                    "eyeColor": "green",
                    "name": "Ballard Griffith",
                    "gender": "male",
                    "company": "COLLAIRE",
                    "email": "ballardgriffith@collaire.com",
                    "phone": "+1 (969) 510-2327",
                    "address": "647 Bowne Street, Greenbush, Hawaii, 4254",
                    "about": "Ad esse enim occaecat dolore consectetur excepteur nulla ad ipsum sit adipisicing incididunt. Mollit qui sint occaecat incididunt exercitation dolore ut adipisicing magna minim enim minim magna enim. Dolor sint aute do commodo officia cillum qui nostrud pariatur esse tempor minim. Laborum dolor duis commodo qui duis proident id sint adipisicing veniam velit culpa fugiat incididunt. Ut sit eiusmod ullamco Lorem ipsum nisi commodo mollit est amet dolor est adipisicing incididunt. Non amet minim qui ad eu reprehenderit est consequat ipsum deserunt commodo labore reprehenderit.\\r\\n",
                    "registered": "2019-04-05T06:34:38 -06:-30",
                    "latitude": 17.343247,
                    "longitude": 6.829427,
                    "tags": [
                      "elit",
                      "ad",
                      "excepteur",
                      "incididunt",
                      "do",
                      "commodo",
                      "aute"
                    ],
                    "friends": [
                      {
                        "id": 0,
                        "name": "Julianne Battle"
                      },
                      {
                        "id": 1,
                        "name": "James Hurley"
                      },
                      {
                        "id": 2,
                        "name": "Yates Munoz"
                      }
                    ],
                    "greeting": "Hello, Ballard Griffith! You have 4 unread messages.",
                    "favoriteFruit": "apple"
                  }
                """;
    }
}