package com.firas.saas.billing.strategy;

import com.firas.saas.billing.dto.PaymentRequest;

public interface PaymentStrategy {
    boolean processPayment(PaymentRequest request);
    String getStrategyName(); // e.g. "MOCK", "STRIPE"
}
