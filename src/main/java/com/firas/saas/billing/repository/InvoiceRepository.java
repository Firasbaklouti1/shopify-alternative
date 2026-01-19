package com.firas.saas.billing.repository;

import com.firas.saas.billing.entity.Invoice;
import com.firas.saas.common.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends BaseRepository<Invoice> {
    List<Invoice> findByTenantId(Long tenantId);
}
