package dev.pravin.workflow.controller;

import dev.pravin.workflow.lamf.Constants;
import dev.pravin.workflow.lamf.LamfOrchestrationWorkflow;
import dev.pravin.workflow.lamf.request.GetStartedRequest;
import dev.pravin.workflow.lamf.request.SelectedMutualFundRequest;
import dev.pravin.workflow.lamf.request.ValidateOtpRequest;
import dev.pravin.workflow.lamf.response.Response;
import io.iworkflow.core.Client;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/lamf-workflow")
public class LamfWorkflowController {

    private final Client client;

    public LamfWorkflowController(Client client) {
        this.client = client;
    }

    @PostMapping("/start")
    ResponseEntity<Response> start(@RequestParam String customerId) {
        client.startWorkflow(LamfOrchestrationWorkflow.class, customerId, 3600);
        return ResponseEntity.ok(new Response("", "success", ""));
    }

    @PostMapping("/consent")
    ResponseEntity<Response> saveConsent(@RequestBody GetStartedRequest getStartedRequest) {
        client.signalWorkflow(LamfOrchestrationWorkflow.class, getStartedRequest.customerId(),
                Constants.SC_USER_INPUT_CONSENT,getStartedRequest);
        return ResponseEntity.ok(new Response("", "success", ""));
    }

    @PostMapping("/validate/otp")
    ResponseEntity<Response> validateOtp(@RequestBody ValidateOtpRequest validateOtpRequest) {
        client.signalWorkflow(LamfOrchestrationWorkflow.class, validateOtpRequest.customerId(),
                Constants.SC_USER_INPUT_MF_PULL_OTP, validateOtpRequest);
        return ResponseEntity.ok(new Response("", "success", ""));
    }

    @PostMapping("/schemes")
    ResponseEntity<Response> schemes(@RequestBody SelectedMutualFundRequest selectedMutualFundRequest) {
        client.signalWorkflow(LamfOrchestrationWorkflow.class, selectedMutualFundRequest.customerId(),
                Constants.SC_USER_INPUT_MF_SCHEME_LIST,selectedMutualFundRequest.selectedMutualFund());
        return ResponseEntity.ok(new Response("", "success", ""));
    }
}
