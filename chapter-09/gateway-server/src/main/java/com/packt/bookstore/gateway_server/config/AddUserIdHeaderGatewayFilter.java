package com.packt.bookstore.gateway_server.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Gateway filter that extracts user information from JWT token and adds it as
 * custom headers.
 * 
 * This filter:
 * - Strips any client-supplied X-User-Id/X-User-Email/X-User-Name headers so
 * they cannot be spoofed by callers
 * - Extracts JWT token from the security context
 * - Gets user information: keycloakId (subject), email, and preferred_username
 * - Sets these as custom headers (X-User-Id, X-User-Email, X-User-Name) on the
 * request, derived solely from the validated JWT
 * - Allows downstream microservices to access authenticated user information
 * 
 * This is used for routes that need user context, such as /api/users/profile
 * and /api/inventory/**
 */
@Component
@Slf4j
public class AddUserIdHeaderGatewayFilter extends AbstractGatewayFilterFactory<Object> {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_NAME = "X-User-Name";

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
            // Always strip these headers first so a caller can never inject its own identity
            var strippedExchange = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.remove(HEADER_USER_ID);
                        headers.remove(HEADER_USER_EMAIL);
                        headers.remove(HEADER_USER_NAME);
                    }))
                    .build();

            // Get the security context and extract user information from JWT
            return ReactiveSecurityContextHolder.getContext()
                    // Extract the authentication object from security context
                    .map(securityContext -> securityContext.getAuthentication())
                    // Filter to ensure it's a JwtAuthenticationToken
                    .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                    // Cast to JwtAuthenticationToken
                    .map(authentication -> (JwtAuthenticationToken) authentication)
                    // Extract user information from JWT and set as headers
                    .map(jwtAuth -> {
                        // Get the JWT token
                        Jwt jwt = jwtAuth.getToken();

                        // Extract user information from JWT claims
                        String keycloakId = jwt.getSubject(); // 'sub' claim
                        String email = jwt.getClaimAsString("email"); // 'email' claim
                        String preferredUsername = jwt.getClaimAsString("preferred_username"); // 'preferred_username'
                                                                                               // claim

                        // Mutate the stripped exchange to set the validated headers
                        return strippedExchange.mutate()
                                .request(r -> r
                                        .header(HEADER_USER_ID, keycloakId) // Keycloak user ID
                                        .header(HEADER_USER_EMAIL, email) // User email
                                        .header(HEADER_USER_NAME, preferredUsername) // User's preferred username
                        )
                                .build();
                    })
                    // If no authentication found, continue with the stripped exchange (no spoofed headers)
                    .defaultIfEmpty(strippedExchange)
                    // Continue the filter chain with the modified exchange
                    .flatMap(chain::filter);
        };
    }
}
