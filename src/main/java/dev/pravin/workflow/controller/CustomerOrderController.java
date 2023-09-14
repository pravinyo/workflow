package dev.pravin.workflow.controller;

import dev.pravin.workflow.shop.Constants;
import dev.pravin.workflow.shop.model.Order;
import dev.pravin.workflow.shop.order.CustomerOrderOrchestrationWorkflow;
import dev.pravin.workflow.shop.request.OrderRequest;
import dev.pravin.workflow.shop.request.PaymentStatus;
import dev.pravin.workflow.shop.scheduler.StaleOrderNotificationWorkflow;
import io.iworkflow.core.Client;
import io.iworkflow.core.ClientSideException;
import io.iworkflow.core.ImmutableResetWorkflowTypeAndOptions;
import io.iworkflow.core.WorkflowOptions;
import io.iworkflow.gen.models.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import static io.iworkflow.gen.models.ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS;
import static io.iworkflow.gen.models.ErrorSubStatus.WORKFLOW_NOT_EXISTS_SUB_STATUS;

@RestController
@RequestMapping("/order")
public class CustomerOrderController {
    private final Client client;

    public CustomerOrderController(Client client) {
        this.client = client;
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createOrder(
            @RequestBody OrderRequest orderRequest
    ) {
        try {
            var workflowId = orderRequest.getCustomerId() +"-"+ System.currentTimeMillis() / 1000;
            client.startWorkflow(CustomerOrderOrchestrationWorkflow.class, workflowId, 3600, orderRequest);
            var response = String.format("orderId workflowId: %s", workflowId);
            return ResponseEntity.ok(response);

        } catch (ClientSideException e) {
            if (e.getErrorSubStatus() == WORKFLOW_NOT_EXISTS_SUB_STATUS) {
                return ResponseEntity.internalServerError()
                        .body("Workflow doesn't exist");
            }else if (e.getErrorSubStatus() != WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                throw e;
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/details")
    public ResponseEntity<Order> getOrder(
            @RequestParam String workflowId) {
        var rpcStub = client.newRpcStub(CustomerOrderOrchestrationWorkflow.class, workflowId);
        var order = client.invokeRPC(rpcStub::getOrder);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/payment-status")
    public ResponseEntity<String> paymentStatus(
            @RequestBody PaymentStatus paymentStatus
            ) {
        if (paymentStatus.getIsSuccessful()) {
            client.signalWorkflow(CustomerOrderOrchestrationWorkflow.class,
                    paymentStatus.getWorkflowId(), Constants.SC_PAYMENT_SUCCESS_SIGNAL, paymentStatus.getTransactionId());
        } else {
            client.signalWorkflow(CustomerOrderOrchestrationWorkflow.class,
                    paymentStatus.getWorkflowId(), Constants.SC_PAYMENT_FAILED_SIGNAL, null);
        }
        return ResponseEntity.ok("done");
    }

    @PostMapping("/scheduler/notify-stale-order-job")
    public ResponseEntity<String> schedule() {
        try {
            var workflowId = "STALE-ORDER-NOTIFICATION-JOB";
            var workflowOption = new WorkflowOptions() {
                @Override
                public Optional<IDReusePolicy> getWorkflowIdReusePolicy() {
                    return Optional.of(IDReusePolicy.ALLOW_IF_NO_RUNNING);
                }

                @Override
                public Optional<String> getCronSchedule() {
                    return Optional.of("*/5 * * * *");
                }

                @Override
                public Optional<WorkflowRetryPolicy> getWorkflowRetryPolicy() {
                    return Optional.of(new WorkflowRetryPolicy()
                            .backoffCoefficient(2f)
                            .maximumAttempts(3)
                            .initialIntervalSeconds(3)
                            .maximumIntervalSeconds(60));
                }

                @Override
                public Map<String, Object> getInitialSearchAttribute() {
                    return Map.of();
                }

                @Override
                public Optional<WorkflowConfig> getWorkflowConfigOverride() {
                    return Optional.empty();
                }
            };

            client.startWorkflow(StaleOrderNotificationWorkflow.class, workflowId, 3600,null, workflowOption);
            return ResponseEntity.ok("Scheduled");

        } catch (ClientSideException e) {
            if (e.getErrorSubStatus() == WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                return ResponseEntity.internalServerError()
                        .body("Workflow already running");
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/workflow/stop")
    public ResponseEntity<String> stopWorkflow(
            @RequestParam String workflowId
    ) {
        try {
            var workflowDesc = client.describeWorkflow(workflowId);
            if (workflowDesc.getWorkflowStatus().equals(WorkflowStatus.RUNNING)) {
                client.stopWorkflow(workflowId);
                return ResponseEntity.ok("Stopped");
            }
            return ResponseEntity.badRequest().body("Workflow ID invalid");

        } catch (ClientSideException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/state/reset")
    public ResponseEntity<String> getWorkflowResults(
            @RequestParam String workflowId,
            @RequestParam int eventId
    ) {
        try {
            var resetWorkflowTypeAndOptions = ImmutableResetWorkflowTypeAndOptions
                    .builder()
                    .resetType(WorkflowResetType.HISTORY_EVENT_ID)
                    .skipSignalReapply(Optional.of(true))
                    .reason("resetting to previous flow")
                    .historyEventId(Optional.of(eventId))
                    .build();

            var result = client.resetWorkflow(workflowId, resetWorkflowTypeAndOptions);
            return ResponseEntity.ok(result);
        } catch (ClientSideException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/change")
    public ResponseEntity<String> changeOrder(
            @RequestParam String workflowId,
            @RequestBody OrderRequest orderRequest
    ) {
        try {
            var rpcStub = client.newRpcStub(CustomerOrderOrchestrationWorkflow.class, workflowId);
            client.invokeRPC(rpcStub::changeOrder, orderRequest);
            return ResponseEntity.ok("Done");
        } catch (ClientSideException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
