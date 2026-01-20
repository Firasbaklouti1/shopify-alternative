package com.firas.saas.billing.repository;

import com.firas.saas.billing.entity.Payment;
import com.firas.saas.common.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends BaseRepository<Payment> {

    List<Payment> findByTenantId(Long tenantId);

    List<Payment> findByInvoiceIdAndTenantId(Long invoiceId, Long tenantId);

    Optional<Payment> findByTransactionIdAndTenantId(String transactionId, Long tenantId);

    Optional<Payment> findByPaymentIntentIdAndTenantId(String paymentIntentId, Long tenantId);
}

