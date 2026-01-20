package com.firas.saas.order.entity;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /**
     * Returns the set of valid next statuses from the current status.
     * Enforces business rules for order state machine.
     */
    public Set<OrderStatus> getValidTransitions() {
        return switch (this) {
            case PENDING -> EnumSet.of(PAID, CANCELLED);
            case PAID -> EnumSet.of(PROCESSING, SHIPPED, CANCELLED);
            case PROCESSING -> EnumSet.of(SHIPPED, CANCELLED);
            case SHIPPED -> EnumSet.of(DELIVERED);
            case DELIVERED -> EnumSet.noneOf(OrderStatus.class); // Terminal state
            case CANCELLED -> EnumSet.noneOf(OrderStatus.class); // Terminal state
        };
    }

    /**
     * Checks if transitioning to the target status is valid.
     */
    public boolean canTransitionTo(OrderStatus target) {
        return getValidTransitions().contains(target);
    }
}
