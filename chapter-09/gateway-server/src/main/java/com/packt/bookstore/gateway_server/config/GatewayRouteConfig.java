package com.packt.bookstore.gateway_server.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.RouteMetadataUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayRouteConfig.class);

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring Gateway Routes with Tracing Support");
        return builder.routes()

                // SSE Route for Inventory Notifications (Server-Sent Events)
                // MUST come BEFORE general inventory route to avoid circuit breaker interference
                .route("packt-inventory-notifications", r -> r
                        .path("/packt/inventory/api/notifications/**")
                        .filters(f -> f
                                .rewritePath("/packt/inventory/api/notifications/(?<segment>.*)", 
                                            "/inventory/api/notifications/${segment}")
                                .addResponseHeader("X-Processed-By", "Spring-Gateway")
                                .addRequestHeader("X-Gateway-Trace", "notification-route")
                        )
                        // Set 1-hour timeout for SSE connections
                        .metadata(RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR, Duration.ofHours(1).toMillis())
                        .uri("lb://inventory-service")
                )

                // PredicateEvaluator + PreFilter + URI Resolution
                .route("packt-inventory-service", r -> r
                        .path("/packt/inventory/api/**") // PredicateEvaluator
                        .filters(f -> f
                                .rewritePath("/packt/inventory/api/(?<segment>.*)", "/inventory/api/${segment}") // PreFilter
                                .addResponseHeader("X-Processed-By", "Spring-Gateway") // PostFilter
                                .addRequestHeader("X-Gateway-Trace", "inventory-route") // Tracing support
                                .filter((exchange, chain) -> {
                                    // Log tracing information
                                    String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
                                    if (traceId != null) {
                                        log.debug("Processing request with trace ID: {}", traceId);
                                    }
                                    return chain.filter(exchange);
                                })
                                .circuitBreaker(config -> config
                                        .setName("inventoryCB")
                                        .setFallbackUri("forward:/fallback/inventory")
                                        .setRouteId("packt-inventory-service")
                                        )
                        )
                        .uri("lb://inventory-service") // HandlerMapping - service discovery
                )

                .route("packt-user-service", r -> r
                        .path("/packt/user/api/**")
                        .filters(f -> f
                                .rewritePath("/packt/user/api/(?<segment>.*)", "/user/api/${segment}")
                                .addResponseHeader("X-Processed-By", "Spring-Gateway")
                                .addRequestHeader("X-Gateway-Trace", "user-route")
                                .filter((exchange, chain) -> {
                                    // Log tracing information
                                    String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
                                    if (traceId != null) {
                                        log.debug("Processing user request with trace ID: {}", traceId);
                                    }
                                    return chain.filter(exchange);
                                }))
                        .uri("lb://user-ms") // HandlerMapping
                )

                .build();
    }

}