package com.packt.bookstore.gateway_server.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Gateway filter that extracts user information from JWT token and adds it as custom headers.
 * 
 * This filter:
 * - Extracts JWT token from the security context
 * - Gets user information: keycloakId (subject), email, and preferred_username
 * - Adds these as custom headers (X-User-Id, X-User-Email, X-User-Name) to the request
 * - Allows downstream microservices to access authenticated user information
 * 
 * This is used for routes that need user context, such as /api/users/profile and /api/inventory/**
 */
@Component
@Slf4j
public class AddUserIdHeaderGatewayFilter extends AbstractGatewayFilterFactory<Object> {

    /**
     * Constructor initializing the filter factory with Object.class config.
     */
    public AddUserIdHeaderGatewayFilter() {
        super(Object.class);
    }

    /**
     * Applies the gateway filter to add user headers from JWT token.
     * 
     * @param config the configuration object (not used in this implementation)
     * @return a GatewayFilter that adds user headers to requests
     */
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            // Get the security context and extract user information from JWT
            return ReactiveSecurityContextHolder.getContext()
                // Extract the authentication object from security context
                .map(securityContext -> securityContext.getAuthentication())
                // Filter to ensure it's a JwtAuthenticationToken
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                // Cast to JwtAuthenticationToken
                .map(authentication -> (JwtAuthenticationToken) authentication)
                // Extract user information from JWT and add as headers
                .map(jwtAuth -> {
                    // Get the JWT token
                    Jwt jwt = jwtAuth.getToken();

                    // Extract user information from JWT claims
                    String keycloakId = jwt.getSubject();                                    // 'sub' claim
                    String email = jwt.getClaimAsString("email");                           // 'email' claim
                    String preferredUsername = jwt.getClaimAsString("preferred_username"); // 'preferred_username' claim

                    // Mutate the exchange to add custom headers
                    return exchange.mutate()
                        .request(r -> r
                            .header("X-User-Id", keycloakId)           // Keycloak user ID
                            .header("X-User-Email", email)             // User email
                            .header("X-User-Name", preferredUsername)  // User's preferred username
                        )
                        .build();
                })
                // If no authentication found, return the original exchange
                .defaultIfEmpty(exchange)
                // Continue the filter chain with the modified (or original) exchange
                .flatMap(chain::filter);
        };
    }
}
