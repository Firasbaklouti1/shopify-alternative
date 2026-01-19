package com.firas.saas.billing.service;

import com.firas.saas.billing.dto.PaymentRequest;
import com.firas.saas.billing.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentService(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PaymentStrategy::getStrategyName, Function.identity()));
    }

    public boolean processPayment(PaymentRequest request) {
        String method = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "MOCK";
        PaymentStrategy strategy = strategies.get(method);
        
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
        
        return strategy.processPayment(request);
    }
}
