package com.firas.saas.app.security;

import com.firas.saas.app.entity.AppScope;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect to enforce @RequiresScope annotations on controller methods.
 * Checks if the authenticated app has the required scopes.
 */
@Aspect
@Component
@Slf4j
public class ScopeEnforcementAspect {

    @Around("@annotation(com.firas.saas.app.security.RequiresScope)")
    public Object enforceScope(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AppPrincipal)) {
            log.debug("No AppPrincipal found in security context");
            throw new AccessDeniedException("App authentication required");
        }

        AppPrincipal principal = (AppPrincipal) authentication.getPrincipal();

        // Get the RequiresScope annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresScope requiresScope = method.getAnnotation(RequiresScope.class);

        if (requiresScope == null) {
            return joinPoint.proceed();
        }

        AppScope[] requiredScopes = requiresScope.value();
        boolean requireAll = requiresScope.requireAll();

        boolean hasAccess;
        if (requireAll) {
            hasAccess = principal.hasAllScopes(requiredScopes);
        } else {
            hasAccess = principal.hasAnyScope(requiredScopes);
        }

        if (!hasAccess) {
            log.warn("App {} denied access to {} - missing required scopes: {}",
                    principal.getClientId(),
                    method.getName(),
                    requiredScopes);
            throw new AccessDeniedException("Missing required scope(s): " + formatScopes(requiredScopes));
        }

        log.debug("App {} authorized for {} with scopes: {}",
                principal.getClientId(),
                method.getName(),
                principal.getScopes());

        return joinPoint.proceed();
    }

    private String formatScopes(AppScope[] scopes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scopes.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(scopes[i].name());
        }
        return sb.toString();
    }
}
