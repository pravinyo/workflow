package dev.pravin.workflow.shop.order;

import dev.pravin.workflow.shop.model.Order;
import dev.pravin.workflow.shop.order.steps.*;
import dev.pravin.workflow.shop.request.OrderRequest;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.communication.*;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static dev.pravin.workflow.shop.Constants.*;

/*
      Steps
      enterShop
      .next(orderProduct)
      .next(placeOrder)
      .next(makePayment, dependsOn= placeOrder)
      .next(checkPaymentStatus)
      .next(createInvoice, dependsOn = checkPaymentStatus)
      .next(sendCommunication)
      val placeOrder = decisionBranch(WITH_EXCHANGE= flow1,
      WITHOUT_EXCHANGE= flow2)
      val flow1 = parallel(placeShippingOrder, placeExchangeOrder)
      val flow2 = placeShippingOrder

      if payment failed, ask again for the payment
      if payment success, use the transaction Id to generate invoice

     */

@Component
public class CustomerOrderOrchestrationWorkflow implements ObjectWorkflow {
    private final List<StateDef> stateDefs;

    public CustomerOrderOrchestrationWorkflow() {
        this.stateDefs = List.of(
                StateDef.startingState(new OrderStep()),
                StateDef.nonStartingState(new ValidateOrderStep()),
                StateDef.nonStartingState(new ShippingOrderStep()),
                StateDef.nonStartingState(new ExchangeOrderStep()),
                StateDef.nonStartingState(new PaymentStep()),
                StateDef.nonStartingState(new PaymentStatusStep()),
                StateDef.nonStartingState(new InvoiceStep())
        );
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return stateDefs;
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
         return List.of(
                 DataAttributeDef.create(Order.class, DA_CUSTOMER_ORDER_KEY),
                 DataAttributeDef.create(SignalCommandResult.class, DA_SIGNAL_COMMAND_KEY)
         );
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return List.of(
                InternalChannelDef.create(String.class, TOPIC_ORDER_DISPATCHED),
                InternalChannelDef.create(String.class, TOPIC_ORDER_EXCHANGE_CREATED),
                InternalChannelDef.create(OrderRequest.class, IC_ORDER_CHANGED),
                SignalChannelDef.create(String.class, SC_PAYMENT_SUCCESS_SIGNAL),
                SignalChannelDef.create(Void.class, SC_PAYMENT_FAILED_SIGNAL)
        );
    }

    @RPC
    public void changeOrder(Context context, OrderRequest orderRequest, Persistence persistence, Communication communication) {
        communication.publishInternalChannel(IC_ORDER_CHANGED, orderRequest);
    }

    @RPC
    public Order getOrder(Context context, Persistence persistence, Communication communication) {
        var order = persistence.getDataAttribute(DA_CUSTOMER_ORDER_KEY, Order.class);
        if (Objects.isNull(order)){
            return getEmptyOrder();
        }
        return order;
    }

    private Order getEmptyOrder() {
        return Order.builder().build();
    }
}
