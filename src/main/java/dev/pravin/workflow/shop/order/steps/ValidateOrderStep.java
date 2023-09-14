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

import java.util.Random;

public class ValidateOrderStep implements WorkflowState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public StateDecision execute(Context context, Void orderRequest, CommandResults commandResults, Persistence persistence, Communication communication) {
        var order = persistence.getDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, Order.class);

        // verify in the inventory
        var isVerified = new Random().nextBoolean();
        // for simplicity, lets assume all item will be either available or not available as whole.
        if (isVerified) {
            return orderConfirmedAndReturnNextState(persistence, order);
        } else {
            return orderUnavailableAndReturnNextState(persistence, order);
        }
    }

    private StateDecision orderUnavailableAndReturnNextState(Persistence persistence, Order order) {
        order.setStatus(OrderStatus.NOT_AVAILABLE);
        persistence.setDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, order);
        return getNextStep(order);
    }

    private StateDecision orderConfirmedAndReturnNextState(Persistence persistence, Order order) {
        order.setStatus(OrderStatus.CONFIRMED);
        persistence.setDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, order);
        return getNextStep(order);
    }

    private StateDecision getNextStep(Order order) {
        if (order.getStatus() == OrderStatus.NOT_AVAILABLE) {
            return StateDecision.forceCompleteWorkflow("Done Invalid");

        } else if (order.isWithExchange()) {
            return StateDecision.multiNextStates(ShippingOrderStep.class, ExchangeOrderStep.class);

        } else {
            return StateDecision.singleNextState(ShippingOrderStep.class);
        }
    }
}
