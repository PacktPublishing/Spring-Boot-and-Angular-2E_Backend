package com.packt.bookstore.gateway_server.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // PredicateEvaluator + PreFilter + URI Resolution
                .route("packt-inventory-service", r -> r
                        .path("/packt/inventory/api/**") // PredicateEvaluator
                        .filters(f -> f
                                .rewritePath("/packt/inventory/api/(?<segment>.*)", "/inventory/api/${segment}") // PreFilter
                                .addResponseHeader("X-Processed-By", "Spring-Gateway") // PostFilter
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
                                .rewritePath("/packt/user/api/(?<segment>.*)", "/user/api/${segment}"))
                        .uri("lb://user-ms") // HandlerMapping
                )

                .build();
    }

}