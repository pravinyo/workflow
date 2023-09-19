package dev.pravin.workflow.controller;

import dev.pravin.workflow.kyc.AadhaarKycWorkflow;
import dev.pravin.workflow.lamf.Constants;
import dev.pravin.workflow.lamf.LamfOrchestrationWorkflow;
import dev.pravin.workflow.lamf.request.GetStartedRequest;
import dev.pravin.workflow.lamf.request.InitiateKycRequest;
import dev.pravin.workflow.lamf.request.SelectedMutualFundRequest;
import dev.pravin.workflow.lamf.request.ValidateOtpRequest;
import dev.pravin.workflow.lamf.response.Response;
import io.iworkflow.core.Client;
import io.iworkflow.core.ImmutableWorkflowOptions;
import io.iworkflow.gen.models.WorkflowSearchResponse;
import io.iworkflow.gen.models.WorkflowStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/lamf-workflow")
public class LamfWorkflowController {

    private final Client client;

    public LamfWorkflowController(Client client) {
        this.client = client;
    }

    @PostMapping("/start")
    ResponseEntity<Response> start(@RequestParam String customerId) {
        var workflowId = getWorkflowId(customerId);
        var options = ImmutableWorkflowOptions.builder()
                .initialSearchAttribute(Map.of(Constants.SA_CUSTOMER_ID, customerId))
                .build();

        client.startWorkflow(LamfOrchestrationWorkflow.class, workflowId, 3600, null, options);
        return ResponseEntity.ok(new Response( "success", ""));
    }

    private String getWorkflowId(String customerId) {
        return "WF-LAMF-"+customerId;
    }

    @PostMapping("/consent")
    ResponseEntity<Response> saveConsent(@RequestBody GetStartedRequest getStartedRequest) {
        var workflowId = getWorkflowId(getStartedRequest.customerId());
        client.signalWorkflow(LamfOrchestrationWorkflow.class, workflowId,
                Constants.SC_USER_INPUT_CONSENT,getStartedRequest);
        return ResponseEntity.ok(new Response( "success", ""));
    }

    @PostMapping("/validate/otp")
    ResponseEntity<Response> validateOtp(@RequestBody ValidateOtpRequest validateOtpRequest) {
        var workflowId = getWorkflowId(validateOtpRequest.customerId());
        client.signalWorkflow(LamfOrchestrationWorkflow.class, workflowId,
                Constants.SC_USER_INPUT_MF_PULL_OTP, validateOtpRequest);
        return ResponseEntity.ok(new Response( "success", ""));
    }

    @PostMapping("/schemes")
    ResponseEntity<Response> schemes(@RequestBody SelectedMutualFundRequest selectedMutualFundRequest) {
        var workflowId = getWorkflowId(selectedMutualFundRequest.customerId());
        client.signalWorkflow(LamfOrchestrationWorkflow.class, workflowId,
                Constants.SC_USER_INPUT_MF_SCHEME_LIST,selectedMutualFundRequest.selectedMutualFund());
        return ResponseEntity.ok(new Response( "success", ""));
    }

    @GetMapping("/search")
    ResponseEntity<Response> getQuery(@RequestParam String query) {
        query = escapeQuote(query);
        System.out.println("got query for search: " + query);
        WorkflowSearchResponse response = client.searchWorkflow(query, 1000);
        return ResponseEntity.ok(new Response( response, ""));
    }

    String escapeQuote(String input) {
        if (input.startsWith("'")) {
            input = input.substring(1, input.length() - 1);
        }
        if (input.startsWith("\"")) {
            input = input.substring(1, input.length() - 1);
        }
        return input;
    }

    @GetMapping("/complex/result")
    ResponseEntity<Response> getResult(@RequestParam String customerId) {
        var workflowId = getWorkflowId(customerId);
        var response = client.getComplexWorkflowResultWithWait(workflowId);
        return ResponseEntity.ok(new Response( response, ""));
    }

    @GetMapping("/status")
    ResponseEntity<Response> status(@RequestParam String customerId) {
        var workflowId = getWorkflowId(customerId);
        var response = client.describeWorkflow(workflowId);
        return ResponseEntity.ok(new Response( response, ""));
    }

    @GetMapping("/search-attributes")
    ResponseEntity<Response> getAllSearchAttributes(@RequestParam String customerId) {
        var workflowId = getWorkflowId(customerId);
        var response = client.getAllSearchAttributes(LamfOrchestrationWorkflow.class, workflowId);
        return ResponseEntity.ok(new Response( response, ""));
    }

    @GetMapping("/data-attributes")
    ResponseEntity<Response> getDataAttributesByWorkflow(@RequestParam String workflowId) {
        var response = client.getAllDataAttributes(
                LamfOrchestrationWorkflow.class,
                workflowId);
        return ResponseEntity.ok(new Response( response, ""));
    }

    @PostMapping("/v2/kyc/aadhaar")
    ResponseEntity<Response> startAadhaarKycV2(@RequestBody InitiateKycRequest initiateKycRequest) {
        var workflowId = getWorkflowId(initiateKycRequest.customerId());
        client.signalWorkflow(LamfOrchestrationWorkflow.class, workflowId, Constants.SC_USER_INPUT_KYC, initiateKycRequest);
        return ResponseEntity.ok(new Response("success", ""));
    }

    @PostMapping("/kyc/aadhaar")
    ResponseEntity<Response> startAadhaarKyc(
            @RequestParam String aadhaarId,
            @RequestParam String customerId) {
        var workflowId = getWorkflowIdForAadhaar(customerId);
        try {
            var response = client.describeWorkflow(workflowId);

            if (response.getWorkflowStatus().equals(WorkflowStatus.RUNNING)) {
                return ResponseEntity.internalServerError().body(new Response("Workflow already running", ""));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        var options = ImmutableWorkflowOptions.builder()
                .initialSearchAttribute(Map.of(Constants.SA_CUSTOMER_ID, customerId))
                .build();
        client.startWorkflow(AadhaarKycWorkflow.class, workflowId, 3600, aadhaarId, options);
        return ResponseEntity.ok(new Response("success", ""));
    }

    @PostMapping("/kyc/aadhaar/otp")
    ResponseEntity<Response> validateAadhaarOtp(
            @RequestParam String otp,
            @RequestParam String customerId) {
        var workflowId = getWorkflowIdForAadhaar(customerId);
        var response = client.describeWorkflow(workflowId);

        if (response.getWorkflowStatus().equals(WorkflowStatus.RUNNING)) {
            client.signalWorkflow(AadhaarKycWorkflow.class,
                    workflowId, "AadhaarOtpSignal", otp);
            return ResponseEntity.ok(new Response("success", ""));
        }
        return ResponseEntity.internalServerError().body(new Response("Workflow not running", ""));
    }

    private String getWorkflowIdForAadhaar(String customerId) {
        return "WF-LAMF-KYC-"+customerId;
    }
}
