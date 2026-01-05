package com.packt.bookstore.gateway_server.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Hooks;

/**
 * Configuration to enable automatic MDC propagation in reactive WebFlux context.
 * This is critical for trace IDs to appear in logs for Spring Cloud Gateway.
 */
@Configuration
public class ObservabilityConfig {

    /**
     * Enable automatic context propagation in Project Reactor.
     * This allows Micrometer tracing context (including trace IDs) to be 
     * automatically propagated to SLF4J MDC in reactive chains.
     */
    @PostConstruct
    public void enableAutomaticContextPropagation() {
        Hooks.enableAutomaticContextPropagation();
    }
}
