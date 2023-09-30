package dev.pravin.workflow.kyc;

import dev.pravin.workflow.lamf.Constants;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class ValidateAadhaarOtpStep implements WorkflowState<String> {
    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, String input, Persistence persistence, Communication communication) {
        return CommandRequest.forAllCommandCompleted(
                SignalCommand.create(Constants.SC_AADHAAR_OTP_SIGNAL)
        );
    }

    @Override
    public StateDecision execute(Context context, String aadhaarReferenceId, CommandResults commandResults, Persistence persistence, Communication communication) {
        var otp = (String) commandResults.getSignalValueByIndex(0);
        if (validateOtp(aadhaarReferenceId, otp)) {
            var details = fetchAadhaarDetails(aadhaarReferenceId, otp);
            return StateDecision.singleNextState(SaveAadhaarDetailsStep.class, details);

        }

        return StateDecision.singleNextState(ValidateAadhaarOtpStep.class, aadhaarReferenceId);
    }

    private Boolean validateOtp(String aadhaarReferenceId, String otp) {
        log.info("call aadhaar validate OTP API and fetch details for referenceId:{} and OTP:{}", aadhaarReferenceId, otp);
        return Objects.equals(otp, "1234");
    }

    private String fetchAadhaarDetails(String aadhaarReferenceId, String otp) {
        log.info("call aadhaar validate OTP API and fetch details for referenceId:{} and OTP:{}", aadhaarReferenceId, otp);
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
