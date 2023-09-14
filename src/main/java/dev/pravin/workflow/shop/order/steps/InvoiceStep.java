package dev.pravin.workflow.shop.order.steps;

import dev.pravin.workflow.shop.Constants;
import dev.pravin.workflow.shop.model.Order;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

public class InvoiceStep implements WorkflowState<String> {
    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public StateDecision execute(Context context, String transactionId, CommandResults commandResults, Persistence persistence, Communication communication) {
        var order = persistence.getDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, Order.class);
        generateInvoice(order, transactionId);
        return StateDecision.forceCompleteWorkflow("Flow completed");
    }

    private void generateInvoice(Order order, String transactionId) {
        System.out.println("Updated DB for transactionId " + transactionId);
        System.out.println("Invoice sent to email for order Id: " + order.getOrderId());
    }
}
