package com.firas.saas.billing.strategy;

import com.firas.saas.billing.dto.PaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class MockPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean processPayment(PaymentRequest request) {
        // Simulate payment processing
        System.out.println("Processing MOCK payment for amount: " + request.getAmount() + " " + request.getCurrency());
        return true; // Always succeed for mock
    }

    @Override
    public String getStrategyName() {
        return "MOCK";
    }
}
