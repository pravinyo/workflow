package dev.pravin.workflow.shop.model;

public enum OrderStatus {
    NOT_AVAILABLE,
    CREATED,
    CONFIRMED,
    DISPATCHED,
    PAYMENT_STARTED,
    PAYMENT_DONE,
    PAYMENT_FAILED,
    DELIVERED
}
