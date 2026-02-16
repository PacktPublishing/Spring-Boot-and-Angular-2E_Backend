package com.packt.bookstore.gateway_server.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Security configuration for the API Gateway with WebFlux and OAuth2/JWT support.
 * 
 * This configuration:
 * - Enables WebFlux Security for reactive endpoints
 * - Configures CORS for frontend applications
 * - Sets up JWT authentication with Keycloak
 * - Defines authorization rules for different API endpoints
 * - Extracts roles from Keycloak JWT tokens (realm_access.roles)
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for reactive endpoints.
     * 
     * @param http the ServerHttpSecurity object to configure
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // Disable CSRF for stateless API (using JWT instead)
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

            // Configure authorization for different endpoints
            .authorizeExchange(exchange -> exchange

                // Health and info endpoints - publicly accessible
                .pathMatchers("/actuator/health", "/actuator/info")
                    .permitAll()

                // Gateway routes for user service - public endpoints
                .pathMatchers(HttpMethod.POST, "/packt/user/api/users/signup")
                    .permitAll()

                .pathMatchers(HttpMethod.POST, "/packt/user/api/users/signin")
                    .permitAll()

                .pathMatchers(HttpMethod.POST, "/packt/user/api/users/refresh-token")
                    .permitAll()

                // All other gateway user routes - require authentication
                .pathMatchers("/packt/user/api/**")
                    .authenticated()

                // All other gateway inventory routes - require authentication
                .pathMatchers("/packt/inventory/api/**")
                    .authenticated()

                // Direct API routes - public endpoints
                .pathMatchers(HttpMethod.POST, "/api/users/signup")
                    .permitAll()

                // User signin endpoint - public (no authentication required)
                .pathMatchers(HttpMethod.POST, "/api/users/signin")
                    .permitAll()

                // Get user profile - authentication required
                .pathMatchers(HttpMethod.GET, "/api/users/profile")
                    .authenticated()

                // Update user profile - authentication required
                .pathMatchers(HttpMethod.PUT, "/api/users/profile")
                    .authenticated()

                // Get inventory - requires USER, AUTHOR or ADMIN role
                .pathMatchers(HttpMethod.GET, "/api/inventory/**")
                    .hasAnyRole("USER", "AUTHOR", "ADMIN")

                // Create inventory - requires AUTHOR or ADMIN role
                .pathMatchers(HttpMethod.POST, "/api/inventory/**")
                    .hasAnyRole("AUTHOR", "ADMIN")

                // Update inventory - requires AUTHOR or ADMIN role
                .pathMatchers(HttpMethod.PUT, "/api/inventory/**")
                    .hasAnyRole("AUTHOR", "ADMIN")

                // Delete inventory - requires ADMIN role only
                .pathMatchers(HttpMethod.DELETE, "/api/inventory/**")
                    .hasRole("ADMIN")

                // All other /api/** endpoints require authentication
                .pathMatchers("/api/**")
                    .authenticated()

                // All other endpoints are publicly accessible
                .anyExchange()
                    .permitAll()
            )

            // Configure OAuth2 Resource Server with JWT authentication
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )

            .build();
    }

    /**
     * Creates a JWT Authentication Converter that extracts roles from Keycloak JWT tokens.
     * 
     * Keycloak stores roles in the token under: "realm_access" -> "roles" (array)
     * This converter converts those roles to Spring Security GrantedAuthority objects
     * with the "ROLE_" prefix required by Spring Security.
     * 
     * @return ReactiveJwtAuthenticationConverterAdapter configured for Keycloak tokens
     */
    private ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Set custom authorities converter to extract roles from Keycloak JWT
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract realm_access claim from JWT
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            // If realm_access is missing or doesn't contain roles, return empty list
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of();
            }

            // Get roles collection from realm_access
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");

            // Convert role strings to SimpleGrantedAuthority with ROLE_ prefix
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
        });

        // Wrap the converter in a ReactiveJwtAuthenticationConverterAdapter for WebFlux
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    /**
     * CORS configuration bean.
     * Note: CORS is also configured in application.yml under spring.cloud.gateway.globalcors
     * This bean provides additional programmatic CORS configuration if needed.
     * 
     * @return CorsConfigurationSource with CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of(
            "http://localhost:4200",  // Angular
            "http://localhost:3000",  // React
            "http://localhost:5173"   // Vite
        ));
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
