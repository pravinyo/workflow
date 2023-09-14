package dev.pravin.workflow.shop.order.steps;

import dev.pravin.workflow.shop.Constants;
import dev.pravin.workflow.shop.model.Order;
import dev.pravin.workflow.shop.model.OrderStatus;
import dev.pravin.workflow.shop.request.OrderRequest;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderStep implements WorkflowState<OrderRequest> {

    @Override
    public Class<OrderRequest> getInputType() {
        return OrderRequest.class;
    }

    @Override
    public StateDecision execute(Context context, OrderRequest orderRequest, CommandResults commandResults, Persistence persistence, Communication communication) {
        var uuid = UUID.randomUUID();

        var order = Order.builder()
                .customerId(orderRequest.getCustomerId())
                .orderId(uuid.toString())
                .items(orderRequest.getItems())
                .totalAmount(orderRequest.getTotalAmount())
                .withExchange(orderRequest.isWithExchange())
                .timeStamp(LocalDateTime.now())
                .status(OrderStatus.CREATED)
                .build();

        persistence.setDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, order);
        return StateDecision.singleNextState(ValidateOrderStep.class);
    }
}
