package dev.pravin.workflow.shop.scheduler.steps;

import dev.pravin.workflow.shop.model.Item;
import dev.pravin.workflow.shop.model.Order;
import dev.pravin.workflow.shop.scheduler.model.GenerateEmail;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

import java.util.List;

public class FetchAllStaleOrdersStep implements WorkflowState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public StateDecision execute(Context context, Void input, CommandResults commandResults, Persistence persistence, Communication communication) {
        System.out.println("Started with fetching stale orders");
        // db query to fetch all the stale orders
        var staleOrders = List.of(
                Order.builder()
                        .orderId("123")
                        .customerId("SHOP0001")
                        .items(List.of(Item.builder()
                                        .id("LAPTOP-DELL-0001")
                                        .name("Dell Inspiron 14, core i7, 500 SSD")
                                        .price(50000)
                                .build()))
                        .build(),
                Order.builder()
                        .orderId("124")
                        .customerId("SHOP0002")
                        .items(List.of(Item.builder()
                                .id("EARPHONE-JABRA-0001")
                                .name("Jabra lite 7t")
                                .price(14000)
                                .build()))
                        .build()
        );

        var generateEmails = GenerateEmail.builder().orders(staleOrders).build();
        System.out.println("Completed with fetching stale orders");
        return StateDecision.singleNextState(GenerateEmailStep.class, generateEmails);
    }
}
