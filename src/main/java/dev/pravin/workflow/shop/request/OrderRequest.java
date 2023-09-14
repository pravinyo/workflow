package dev.pravin.workflow.shop.request;

import dev.pravin.workflow.shop.model.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String customerId;
    private List<Item> items;
    private float totalAmount;
    private boolean withExchange;
}