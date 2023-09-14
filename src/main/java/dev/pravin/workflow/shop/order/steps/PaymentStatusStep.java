package dev.pravin.workflow.shop.order.steps;

import dev.pravin.workflow.shop.Constants;
import dev.pravin.workflow.shop.model.Order;
import dev.pravin.workflow.shop.model.OrderStatus;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.*;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.gen.models.ChannelRequestStatus;

import java.util.Objects;
import java.util.Optional;

public class PaymentStatusStep implements WorkflowState<Void> {
    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(Context context, Void input, Persistence persistence, Communication communication) {
        return CommandRequest.forAnyCommandCompleted(
                SignalCommand.create(Constants.SC_PAYMENT_SUCCESS_SIGNAL),
                SignalCommand.create(Constants.SC_PAYMENT_FAILED_SIGNAL),
                InternalChannelCommand.create(Constants.IC_ORDER_CHANGED)
        );
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        var order = persistence.getDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, Order.class);
        System.out.println("Got payment status for order Id" + order.getOrderId());

        var orderChangeResult = getOrderChangeRequest(commandResults);
        if (orderChangeResult.isPresent()) {
            System.out.println("User want to change the order" + order.getOrderId());
            if (orderChangeResult.get().getValue().isEmpty()) {
                throw new IllegalArgumentException("Order is missing");
            }
            return StateDecision.singleNextState(OrderStep.class, orderChangeResult.get().getValue().get());
        }

        var paymentStatusResult = getPaymentStatusResult(commandResults);
        if (paymentStatusResult.isPresent()) {
            if (isPaymentSuccess(paymentStatusResult.get())) {
                updateOrderStatus(order, persistence);
                String transactionId = getTransactionId(paymentStatusResult.get());
                return processSuccessPayment(transactionId, order);

            } else if (isPaymentFailed(paymentStatusResult.get())) {
                updateOrderStatus(order, persistence);
                processFailurePayment(order);
            }
        }
        System.out.println("payment failed for order Id" + order.getOrderId());
        return StateDecision.singleNextState(PaymentStatusStep.class);
    }

    private Optional<SignalCommandResult> getPaymentStatusResult(CommandResults commandResults) {
        return commandResults.getAllSignalCommandResults().stream()
                .filter(signalCommandResult -> signalCommandResult.getSignalRequestStatusEnum()==ChannelRequestStatus.RECEIVED)
                .findFirst();
    }

    private Optional<InternalChannelCommandResult> getOrderChangeRequest(CommandResults commandResults) {
        return commandResults.getAllInternalChannelCommandResult()
                .stream()
                .filter(command -> command.getRequestStatusEnum() == ChannelRequestStatus.RECEIVED)
                .findFirst();
    }

    private String getTransactionId(SignalCommandResult paymentStatusResult) {
        var transactionIdOptional = paymentStatusResult.getSignalValue();
        if (transactionIdOptional.isPresent()) {
            return (String) transactionIdOptional.get();
        }
        throw new IllegalArgumentException("Transaction ID is missing");
    }

    private void processFailurePayment(Order order) {
        order.setStatus(OrderStatus.PAYMENT_FAILED);
    }

    private StateDecision processSuccessPayment(String transactionId, Order order) {
        order.setStatus(OrderStatus.PAYMENT_DONE);
        System.out.println("payment successful for order Id" + order.getOrderId());
        return StateDecision.singleNextState(InvoiceStep.class, transactionId);
    }

    private boolean isPaymentFailed(SignalCommandResult paymentStatusResult) {
        return Objects.equals(paymentStatusResult.getSignalChannelName(), Constants.SC_PAYMENT_FAILED_SIGNAL) &&
                Objects.equals(paymentStatusResult.getSignalRequestStatusEnum(), ChannelRequestStatus.RECEIVED);
    }

    private boolean isPaymentSuccess(SignalCommandResult paymentStatusResult) {
        return Objects.equals(paymentStatusResult.getSignalChannelName(), Constants.SC_PAYMENT_SUCCESS_SIGNAL) &&
                Objects.equals(paymentStatusResult.getSignalRequestStatusEnum(), ChannelRequestStatus.RECEIVED);
    }

    private void updateOrderStatus(Order order, Persistence persistence) {
        persistence.setDataAttribute(Constants.DA_CUSTOMER_ORDER_KEY, order);
    }
}
