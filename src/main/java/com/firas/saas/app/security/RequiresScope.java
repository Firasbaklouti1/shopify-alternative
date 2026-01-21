package com.firas.saas.app.security;

import com.firas.saas.app.entity.AppScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require specific app scopes for a controller method.
 * Used in conjunction with AppTokenAuthFilter for scope enforcement.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresScope {

    /**
     * The required scopes. By default, ANY of the scopes is sufficient.
     */
    AppScope[] value();

    /**
     * If true, ALL scopes are required. If false (default), ANY scope is sufficient.
     */
    boolean requireAll() default false;
}
