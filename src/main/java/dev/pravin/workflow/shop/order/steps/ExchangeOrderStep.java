package dev.pravin.workflow.shop.order.steps;

import dev.pravin.workflow.shop.Constants;
import dev.pravin.workflow.shop.model.Order;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

public class ExchangeOrderStep implements WorkflowState<Void> {
    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        var order = persistence.getDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, Order.class);
        sendEmailForOrderStatus(order);
        System.out.println("Order is dispatched");

        communication.publishInternalChannel(Constants.TOPIC_ORDER_EXCHANGE_CREATED, order.getOrderId());
        return StateDecision.singleNextState(PaymentStep.class);
    }

    private void sendEmailForOrderStatus(Order order) {
        var message = " Dear customer, " +
                "Exchange order is placed for  order Id: ${orderId}. " +
                "Our partner will arrive and validate the exchange product and decide what value to offer.";

        message = message.replace("${orderId}", order.getOrderId());

        System.out.println("Email sent: \n" + message);
    }
}
