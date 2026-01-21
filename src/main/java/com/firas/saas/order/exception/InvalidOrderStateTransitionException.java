package com.firas.saas.order.exception;

import com.firas.saas.order.entity.OrderStatus;

public class InvalidOrderStateTransitionException extends RuntimeException {
    public InvalidOrderStateTransitionException(OrderStatus current, OrderStatus target) {
        super(String.format("Invalid order state transition from %s to %s", current, target));
    }
}
