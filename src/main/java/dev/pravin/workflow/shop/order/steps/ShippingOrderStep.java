package dev.pravin.workflow.shop.order.steps;

import dev.pravin.workflow.shop.Constants;
import dev.pravin.workflow.shop.model.Order;
import dev.pravin.workflow.shop.model.OrderStatus;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

public class ShippingOrderStep implements WorkflowState<Void> {
    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        var order = persistence.getDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, Order.class);
        updateOrderStatus(order, persistence);
        sendEmailForOrderStatus(order);
        System.out.println("Order is dispatched");

        communication.publishInternalChannel(Constants.TOPIC_ORDER_DISPATCHED, order.getOrderId());
        return StateDecision.singleNextState(PaymentStep.class);
    }

    private void sendEmailForOrderStatus(Order order) {
        var message = "Dear customer, Your order with ID: ${orderId} is dispatched and arriving to you soon. " +
                "You can pay the amount: ${amount} with following link: http://localhost/payment";

        message = message
                .replace("${orderId}", order.getOrderId())
                .replace("${amount}", String.valueOf(order.getTotalAmount()));

        System.out.println("Email sent: \n" + message);
    }

    private void updateOrderStatus(Order order, Persistence persistence) {
        order.setStatus(OrderStatus.DISPATCHED);
        persistence.setDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, order);
    }
}
