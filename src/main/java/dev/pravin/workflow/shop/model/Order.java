package dev.pravin.workflow.shop.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String customerId;
    private String orderId;
    private List<Item> items;
    private float totalAmount;
    private boolean withExchange;
    private LocalDateTime timeStamp;
    private OrderStatus status;
}