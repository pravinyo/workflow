package dev.pravin.workflow.shop.scheduler.model;

import dev.pravin.workflow.shop.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenerateEmail {
    private List<Order> orders;
}
