# 📘 Chapter 08 — Securing Microservices Using Spring Security & OAuth2

## Chapter Overview

This chapter builds upon the observable microservices from Chapter 07 by implementing **enterprise-grade security** using **OAuth2**, **JWT tokens**, and **Keycloak** as the identity provider.
You will secure the API Gateway and microservices with **role-based access control (RBAC)**, implement **user authentication and authorization**, and ensure that every request in the Bookstore system is authenticated, authorized, and traceable.

---

## ✅ Before You Run This Chapter

Please confirm the required runtime dependencies before running this chapter:

- Confirm the database is started (PostgreSQL and MongoDB for this chapter).
- Confirm any infrastructure dependencies are running (for example Docker services, if used).
- Confirm any dependencies from previous chapters are running as needed for your flow.

### Sequence for Running Databases and Microservices

1. **Start Databases First**
  - Start PostgreSQL (Inventory DB):
    ```bash
    docker run -d \
     --name bookstore-postgres \
     -e POSTGRES_USER=bookstore \
     -e POSTGRES_PASSWORD=bookstore123 \
     -e POSTGRES_DB=inventory \
     -p 5432:5432 \
     postgres:17
    ```
  - Start MongoDB (User DB):
    ```bash
    docker run -d \
     --name bookstore-mongo \
     -e MONGO_INITDB_ROOT_USERNAME=bookstore \
     -e MONGO_INITDB_ROOT_PASSWORD=bookstore123 \
     -e MONGO_INITDB_DATABASE=userDB \
     -p 27017:27017 \
     mongo:8
    ```
  - Confirm both databases are running:
    ```bash
    docker ps | grep bookstore-postgres
    docker ps | grep bookstore-mongo
    ```

2. **Start Zipkin for Distributed Tracing**
  ```bash
  docker run -d -p 9411:9411 openzipkin/zipkin
  ```
  - Access Zipkin UI at: http://localhost:9411

3. **Start Eureka Discovery Server**
  ```bash
  cd eureka-server
  ./mvnw spring-boot:run
  ```
  - Verify at: http://localhost:8761

4. **Start Inventory Microservice**
  ```bash
  cd inventory-ms
  ./mvnw spring-boot:run
  ```

5. **Start User Microservice**
  ```bash
  cd user-ms
  ./mvnw spring-boot:run
  ```

6. **Start Gateway Microservice**
  ```bash
  cd gateway-server
  ./mvnw spring-boot:run
  ```

## 📦 Chapter Source Code Availability

The final source code for this chapter is already uploaded in this directory.

Use this folder as the reference implementation for the completed chapter state.

---

## Table of Contents
- [Understanding Microservices Security](#understanding-microservices-security)
- [OAuth2 & OpenID Connect Fundamentals](#oauth2--openid-connect-fundamentals)
- [Keycloak as Identity Provider](#keycloak-as-identity-provider)
- [Security Architecture Overview](#security-architecture-overview)
- [Securing the API Gateway](#securing-the-api-gateway)
- [Implementing User Authentication](#implementing-user-authentication)
- [Role-Based Access Control (RBAC)](#role-based-access-control-rbac)
- [JWT Token Validation & Propagation](#jwt-token-validation--propagation)
- [User Service Integration with Keycloak](#user-service-integration-with-keycloak)
- [Testing Secured APIs](#testing-secured-apis)
- [Security Best Practices & Pitfalls](#security-best-practices--pitfalls)
- [Summary](#summary)

---

# Understanding Microservices Security

In distributed microservices architectures, security must be:
- **Centralized** at the gateway for consistent enforcement
- **Token-based** for stateless authentication
- **Role-driven** for fine-grained authorization
- **Traceable** for audit and compliance

Security is not optional — it's the foundation of trust in your system.

---

# OAuth2 & OpenID Connect Fundamentals

## OAuth2 Grant Types
| Grant Type | Use Case |
|-----------|----------|
| **Authorization Code** | Web applications with backend |
| **Password Grant** | Trusted first-party clients |
| **Client Credentials** | Service-to-service communication |
| **Refresh Token** | Token renewal without re-authentication |

## OpenID Connect
OpenID Connect extends OAuth2 by adding:
- **ID Token** (JWT) containing user identity claims
- **UserInfo Endpoint** for retrieving user profile
- **Standardized Claims** (sub, email, name, roles)

---

# Keycloak as Identity Provider

Keycloak is an open-source Identity and Access Management solution that provides:

| Feature | Capability |
|---------|-----------|
| **User Management** | Registration, profile management, password reset |
| **Authentication** | Multiple protocols (OAuth2, SAML, OpenID Connect) |
| **Authorization** | Role-based and attribute-based access control |
| **Token Management** | JWT issuance, validation, refresh |
| **Admin API** | Programmatic user and realm management |

## Running Keycloak with Docker

### Running Keycloak with Docker (Standalone)
To start Keycloak for this chapter, use the following command:

```bash
docker run -d \
  --name bookstore-keycloak \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -e KC_HTTP_PORT=8090 \
  -p 8090:8090 \
  quay.io/keycloak/keycloak:26.0.0 \
  start-dev
```

Access Keycloak Admin Console:
```
http://localhost:8090
Username: admin
Password: admin
```

The `bookstore-realm.json` configures:
- Realm: `bookstore`
- Client: `bookstore-gateway`
- Roles: `user`, `author`, `admin`
- Test users with predefined roles

---

# Security Architecture Overview

Our security implementation follows a **centralized authentication, distributed authorization** model:

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ 1. Request with JWT
       ├──────────────────────────────┐
       │                              │
┌──────▼──────────┐          ┌───────▼────────┐
│  API Gateway    │          │   Keycloak     │
│                 │  2. Validate JWT           │
│ - JWT Validation│◄─────────┤  (Public Keys) │
│ - RBAC Rules    │          └────────────────┘
│ - Route Mapping │
└────────┬────────┘
         │ 3. Forward with User Headers
    ┌────┴────┐
    │         │
┌───▼────┐ ┌──▼──────┐
│User MS │ │Inventory│
│        │ │   MS    │
└────────┘ └─────────┘
```

**Key Principles:**
1. **Gateway validates all JWT tokens** using Keycloak's public keys
2. **Gateway enforces authorization rules** based on roles
3. **User identity propagates via headers** (X-User-Id, X-User-Email, X-User-Name)
4. **Downstream services trust the gateway** and don't re-validate tokens

---

# Securing the API Gateway

## Step 1 — Add Security Dependencies
```xml
<!-- Spring Security with WebFlux -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Resource Server with JWT -->
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>

<!-- JWT Support -->
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

## Step 2 — Configure JWT Validation
In `gateway-server/src/main/resources/application.yml`:
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8090/realms/bookstore
          jwk-set-uri: http://localhost:8090/realms/bookstore/protocol/openid-connect/certs

  main:
    allow-bean-definition-overriding: true
  profiles:
    active: dev
  java:
    version: 25
```

## Step 3 — Create Security Configuration
```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
                // Public endpoints
                .pathMatchers(HttpMethod.POST, "/packt/user/api/users/signup").permitAll()
                .pathMatchers(HttpMethod.POST, "/packt/user/api/users/signin").permitAll()
                .pathMatchers(HttpMethod.POST, "/packt/user/api/users/refresh-token").permitAll()
                
                // Protected endpoints
                .pathMatchers("/packt/user/api/**").authenticated()
                .pathMatchers("/packt/inventory/api/**").authenticated()
                
                // Discovery and monitoring
                .pathMatchers("/eureka/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .build();
    }
}
```

## Step 4 — Extract Roles from JWT
Keycloak stores roles in `realm_access.roles`. We need a custom converter:
```java
private ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return Collections.emptyList();
        
        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());
    });
    
    return new ReactiveJwtAuthenticationConverterAdapter(converter);
}
```

## Step 5 — Forward User Identity Headers
Create `AddUserIdHeaderGatewayFilter.java`:
```java
@Component
@Slf4j
public class AddUserIdHeaderGatewayFilter extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> {
                    Jwt jwt = jwtAuth.getToken();
                    
                    ServerHttpRequest modifiedRequest = exchange.getRequest()
                        .mutate()
                        .header("X-User-Id", jwt.getSubject())
                        .header("X-User-Email", jwt.getClaimAsString("email"))
                        .header("X-User-Name", jwt.getClaimAsString("preferred_username"))
                        .build();
                    
                    return exchange.mutate().request(modifiedRequest).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
        };
    }
}
```

---

# Implementing User Authentication

## User Registration (Signup)

The User Service creates users in both Keycloak and the local database.

### Step 1 — Add Keycloak Admin Client
```xml
<dependency>
  <groupId>org.keycloak</groupId>
  <artifactId>keycloak-admin-client</artifactId>
  <version>26.0.0</version>
</dependency>
```

### Step 2 — Configure Keycloak Admin Client
```java
@Configuration
public class KeycloakConfig {
    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.admin-username}")
    private String adminUsername;
    
    @Value("${keycloak.admin-password}")
    private String adminPassword;
    
    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm("master")
            .username(adminUsername)
            .password(adminPassword)
            .clientId("admin-cli")
            .build();
    }
}
```

### Step 3 — Implement User Registration
```java
@Transactional
public UserProfileDTO signUp(SignUpRequest request) {
    // Check if user exists
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new UserAlreadyExistsException("User already exists");
    }
    
    // Create user in Keycloak
    RealmResource realmResource = keycloak.realm(realm);
    UsersResource usersResource = realmResource.users();
    
    UserRepresentation kcUser = new UserRepresentation();
    kcUser.setEnabled(true);
    kcUser.setUsername(request.getEmail());
    kcUser.setEmail(request.getEmail());
    kcUser.setFirstName(request.getFirstName());
    kcUser.setLastName(request.getLastName());
    kcUser.setEmailVerified(true);
    
    Response response = usersResource.create(kcUser);
    String keycloakId = extractKeycloakId(response);
    
    // Set password
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(request.getPassword());
    credential.setTemporary(false);
    usersResource.get(keycloakId).resetPassword(credential);
    
    // Assign default role
    RoleRepresentation userRole = realmResource.roles().get("user").toRepresentation();
    usersResource.get(keycloakId).roles().realmLevel().add(Arrays.asList(userRole));
    
    // Save to local database
    User user = new User();
    user.setKeycloakId(keycloakId);
    user.setEmail(request.getEmail());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    // ... set other fields
    
    return mapToDTO(userRepository.save(user));
}
```

## User Login (Signin)

The User Service exchanges credentials for JWT tokens via Keycloak's token endpoint.

```java
public SignInResponse signIn(SignInRequest request) {
    String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("grant_type", "password");
    formData.add("client_id", clientId);
    formData.add("username", request.getEmail());
    formData.add("password", request.getPassword());
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
    ResponseEntity<Map> response = restTemplate.postForEntity(
        tokenUrl, 
        new HttpEntity<>(formData, headers), 
        Map.class
    );
    
    Map<String, Object> tokenResponse = response.getBody();
    
    // Build response with tokens and user profile
    SignInResponse signInResponse = new SignInResponse();
    signInResponse.setAccessToken((String) tokenResponse.get("access_token"));
    signInResponse.setRefreshToken((String) tokenResponse.get("refresh_token"));
    signInResponse.setTokenType((String) tokenResponse.get("token_type"));
    signInResponse.setExpiresIn((Integer) tokenResponse.get("expires_in"));
    
    // Fetch user profile
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    signInResponse.setUser(mapToDTO(user));
    
    return signInResponse;
}
```

---

# Role-Based Access Control (RBAC)

## Keycloak Roles
| Role | Permissions |
|------|------------|
| **user** | View books, manage own profile |
| **author** | Create/update books and authors |
| **admin** | Full access (create, update, delete) |

## Gateway Authorization Rules
```java
.authorizeExchange(exchange -> exchange
    // User endpoints
    .pathMatchers(HttpMethod.GET, "/packt/user/api/users/me").authenticated()
    .pathMatchers(HttpMethod.PUT, "/packt/user/api/users/profile").authenticated()
    
    // Inventory - Read (USER, AUTHOR, ADMIN)
    .pathMatchers(HttpMethod.GET, "/packt/inventory/api/books/**").hasAnyRole("USER", "AUTHOR", "ADMIN")
    .pathMatchers(HttpMethod.GET, "/packt/inventory/api/authors/**").hasAnyRole("USER", "AUTHOR", "ADMIN")
    
    // Inventory - Write (AUTHOR, ADMIN)
    .pathMatchers(HttpMethod.POST, "/packt/inventory/api/books").hasAnyRole("AUTHOR", "ADMIN")
    .pathMatchers(HttpMethod.PUT, "/packt/inventory/api/books/**").hasAnyRole("AUTHOR", "ADMIN")
    .pathMatchers(HttpMethod.PATCH, "/packt/inventory/api/books/**").hasAnyRole("AUTHOR", "ADMIN")
    
    // Inventory - Delete (ADMIN only)
    .pathMatchers(HttpMethod.DELETE, "/packt/inventory/api/**").hasRole("ADMIN")
)
```

---

# JWT Token Validation & Propagation

## JWT Structure
A JWT token consists of three parts:
1. **Header** — Algorithm and token type
2. **Payload** — Claims (user info, roles, expiration)
3. **Signature** — Cryptographic signature for validation

## Token Validation Flow
```
1. Client sends request with JWT in Authorization header
2. Gateway extracts token from "Bearer <token>"
3. Gateway fetches Keycloak's public keys (cached)
4. Gateway validates token signature and expiration
5. Gateway extracts roles from "realm_access.roles"
6. Gateway checks if user has required role for endpoint
7. Gateway adds X-User-Id, X-User-Email, X-User-Name headers
8. Gateway forwards request to downstream service
```

## Token Claims
```json
{
  "sub": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email": "john.doe@example.com",
  "preferred_username": "john.doe@example.com",
  "realm_access": {
    "roles": ["user", "author"]
  },
  "exp": 1708024800,
  "iat": 1708024500
}
```

---

# User Service Integration with Keycloak

## Controller Endpoints

### Get Current User Profile
```java
@GetMapping("/me")
public ResponseEntity<?> getProfile(
    @RequestHeader(value = "X-User-Id", required = false) String keycloakId
) {
    if (keycloakId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("success", false, "message", "User ID not found"));
    }
    
    UserProfileDTO profile = userService.getProfileByKeycloakId(keycloakId);
    return ResponseEntity.ok(profile);
}
```

### Update User Profile
```java
@PutMapping("/profile")
public ResponseEntity<?> updateProfile(
    @RequestHeader(value = "X-User-Id", required = false) String keycloakId,
    @Valid @RequestBody UpdateUserProfileRequest request
) {
    if (keycloakId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("success", false, "message", "User ID not found"));
    }
    
    UserProfileDTO updated = userService.updateProfile(keycloakId, request);
    return ResponseEntity.ok(updated);
}
```

---

# Testing Secured APIs

## Complete Authentication Flow

### 1. Register New User
```bash
curl -X POST http://localhost:8080/packt/user/api/users/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "phone": "+1-555-0123",
    "address": "123 Main Street",
    "city": "San Francisco",
    "state": "CA",
    "zipCode": "94105",
    "country": "USA"
  }'
```

### 2. Login and Get JWT Token
```bash
TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8080/packt/user/api/users/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
  }')

ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.accessToken')
echo "Access Token: $ACCESS_TOKEN"
```

### 3. Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/packt/user/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 4. Access Books (Requires USER role)
```bash
curl -X GET "http://localhost:8080/packt/inventory/api/books?page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 5. Create Book (Requires AUTHOR or ADMIN role)
```bash
curl -X POST http://localhost:8080/packt/inventory/api/books \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Security in Action",
    "authorId": 1,
    "isbn": "978-1-61729-620-8",
    "price": 44.99,
    "quantity": 20,
    "publishedDate": "2020-10-01",
    "description": "Comprehensive guide to Spring Security"
  }'
```

### 6. Delete Book (Requires ADMIN role)
```bash
curl -X DELETE http://localhost:8080/packt/inventory/api/books/5 \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## Common Error Responses

### 401 Unauthorized (No/Invalid Token)
```json
{
  "error": "unauthorized",
  "message": "Full authentication is required"
}
```

### 403 Forbidden (Insufficient Permissions)
```json
{
  "error": "access_denied",
  "message": "Access Denied"
}
```

---

# Security Best Practices & Pitfalls

## Common Pitfalls
| Issue | Impact |
|-------|--------|
| **Storing secrets in code** | Credential leaks, security breaches |
| **Using weak passwords** | Easy to crack, unauthorized access |
| **Not validating JWT expiration** | Stale tokens remain valid indefinitely |
| **Logging sensitive data** | Compliance violations, data exposure |
| **Missing CORS configuration** | Browser blocks legitimate requests |
| **Overly permissive roles** | Privilege escalation risks |

## Best Practices
| Area | Recommendation |
|------|----------------|
| **Token Management** | Use short-lived access tokens (5-15 min) |
| **Refresh Tokens** | Store securely, rotate frequently |
| **Password Policies** | Enforce complexity, minimum length |
| **Role Design** | Follow principle of least privilege |
| **Secrets Management** | Use environment variables or vaults |
| **API Security** | Rate limiting, input validation, HTTPS |
| **Monitoring** | Log auth events, failed attempts, role changes |
| **Testing** | Test all RBAC rules, token expiration scenarios |

## Security Checklist
✅ Keycloak configured with strong admin password  
✅ JWT tokens validated at gateway  
✅ Roles extracted and mapped correctly  
✅ Authorization rules cover all endpoints  
✅ User identity propagated via headers  
✅ Secrets stored in environment variables  
✅ CORS configured for frontend domains  
✅ HTTPS enabled in production  
✅ Token expiration enforced  
✅ Failed auth attempts logged  

---

# Summary

In this chapter, you implemented:

✔ **OAuth2 & OpenID Connect** authentication with Keycloak  
✔ **JWT token validation** at the API Gateway  
✔ **Role-based access control (RBAC)** for fine-grained authorization  
✔ **User registration and sign-in** with Keycloak Admin API integration  
✔ **Identity propagation** via custom headers to downstream services  
✔ **Secure API endpoints** with proper authentication and authorization  
✔ **Complete authentication flow** from signup to protected resource access  

Your Bookstore microservices are now **secured, authenticated, and authorized**.  
In the next chapter, we will containerize the entire stack and deploy it to production using **Docker** and **Kubernetes**.

---

# Resources & References

## Official Documentation
- [OAuth2 Specification](https://oauth.net/2/) — Complete OAuth 2.0 framework
- [OpenID Connect](https://openid.net/connect/) — Identity layer on top of OAuth2
- [Keycloak Documentation](https://www.keycloak.org/documentation) — Comprehensive Keycloak guides
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2) — Spring OAuth2 tutorial
- [JWT.io](https://jwt.io/) — JWT decoder and documentation

## Additional Resources
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/) — Admin API reference
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/) — Complete Spring Security documentation
- [OWASP Security Cheat Sheet](https://cheatsheetseries.owasp.org/) — Security best practices
- [API Security Best Practices](https://github.com/OWASP/API-Security) — OWASP API Security Project

## Tools & Testing
- [Postman](https://www.postman.com/) — API testing tool
- [Keycloak Docker Images](https://hub.docker.com/r/keycloak/keycloak) — Official Keycloak containers
- [curl Examples](./docs/USER_API_EXAMPLES.md) — Complete API testing examples

## Further Reading
- 📖 [Securing the Microservices](./docs/securing-the-microservices.md) — Detailed security implementation guide
- 📖 [API Documentation](./docs/API_DOCUMENTATION.md) — Complete API reference
- 📖 [User API Examples](./docs/USER_API_EXAMPLES.md) — curl examples for all endpoints

---

