package dev.pravin.workflow.shop.order.steps;

import dev.pravin.workflow.shop.Constants;
import dev.pravin.workflow.shop.model.Order;
import dev.pravin.workflow.shop.model.OrderStatus;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.InternalChannelCommand;
import io.iworkflow.core.persistence.Persistence;

public class PaymentStep implements WorkflowState<Void> {
    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, Void input, Persistence persistence, Communication communication) {
        var order = persistence.getDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, Order.class);
        if (order.isWithExchange()) {
            return CommandRequest.forAllCommandCompleted(
                    InternalChannelCommand.create(Constants.TOPIC_ORDER_DISPATCHED),
                    InternalChannelCommand.create(Constants.TOPIC_ORDER_EXCHANGE_CREATED)
            );
        } else {
            return CommandRequest.forAllCommandCompleted(
                    InternalChannelCommand.create(Constants.TOPIC_ORDER_DISPATCHED)
            );
        }
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        var order = persistence.getDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, Order.class);
        System.out.println("Request payment for order Id" + order.getOrderId());
        updateOrderStatus(order, persistence);
        // navigate the customer to payment app
        return StateDecision.singleNextState(PaymentStatusStep.class);
    }

    private void updateOrderStatus(Order order, Persistence persistence) {
        order.setStatus(OrderStatus.PAYMENT_STARTED);
        persistence.setDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, order);
    }
}
