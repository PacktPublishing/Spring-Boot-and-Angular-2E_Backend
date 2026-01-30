package com.packt.bookstore.gateway_server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

/**
 * Configuration class to ensure proper tracing support in Spring Cloud Gateway
 */
@Configuration
public class TracingConfig {

    private static final Logger log = LoggerFactory.getLogger(TracingConfig.class);

    /**
     * Global filter to log tracing information for all gateway requests
     */
    @Bean
    public GlobalFilter tracingGlobalFilter() {
        return (exchange, chain) -> {
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()))
                            .build())
                    .build();
            
            String path = exchange.getRequest().getPath().toString();
            log.info("Gateway processing request: {} - Method: {}", 
                    path, exchange.getRequest().getMethod());
            
            return chain.filter(mutatedExchange)
                    .doOnSuccess(result -> log.debug("Successfully processed request: {}", path))
                    .doOnError(error -> log.error("Error processing request: {} - Error: {}", path, error.getMessage()));
        };
    }
    
    /**
     * Make sure this filter has high precedence
     */
    @Bean
    public Ordered tracingFilterOrder() {
        return () -> -1; // High precedence
    }
}