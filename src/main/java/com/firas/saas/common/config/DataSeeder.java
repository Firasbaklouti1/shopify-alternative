package com.firas.saas.common.config;

import com.firas.saas.subscription.service.SubscriptionService;
import com.firas.saas.tenant.entity.Tenant;
import com.firas.saas.tenant.repository.TenantRepository;
import com.firas.saas.user.entity.Role;
import com.firas.saas.user.entity.User;
import com.firas.saas.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SubscriptionService subscriptionService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdmin();
        seedSubscriptionPlans();
    }

    private void seedAdmin() {
        if (!userRepository.existsByEmail("admin@saas.com")) {
            // Create System Tenant if not exists
            Tenant systemTenant = tenantRepository.findBySlug("system-admin")
                    .orElseGet(() -> tenantRepository.save(Tenant.builder()
                            .name("System Admin")
                            .slug("system-admin")
                            .ownerEmail("admin@saas.com")
                            .active(true)
                            .build()));

            // Create Admin User
            userRepository.save(User.builder()
                    .email("admin@saas.com")
                    .password(passwordEncoder.encode("Admin123!"))
                    .fullName("Super Admin")
                    .role(Role.ADMIN)
                    .tenant(systemTenant)
                    .enabled(true)
                    .build());
            System.out.println("SEEDER: Admin user created (admin@saas.com / Admin123!)");
        }
    }

    private void seedSubscriptionPlans() {
        subscriptionService.createPlan("Free Tier", "free", 0.0, "MONTHLY", "Basic features, 10 products");
        subscriptionService.createPlan("Basic Plan", "basic", 29.0, "MONTHLY", "Standard features, 100 products");
        subscriptionService.createPlan("Pro Plan", "pro", 79.0, "MONTHLY", "Advanced features, unlimited products");
        subscriptionService.createPlan("Enterprise", "enterprise", 299.0, "MONTHLY", "All features, priority support");
    }
}
