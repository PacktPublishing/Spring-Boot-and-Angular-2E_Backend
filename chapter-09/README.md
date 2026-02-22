# 📘 Chapter 09 — Advanced Security, Observability & Production Readiness

## Chapter Overview

This chapter extends the microservices architecture with advanced security, observability, and production readiness features. You will integrate Keycloak for identity management, enhance distributed tracing, and apply best practices for deploying and monitoring microservices in real-world environments.

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

3. **Start Keycloak for Identity Management**
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
   - Access Keycloak Admin Console:
     - http://localhost:8090
     - Username: admin
     - Password: admin
   - Import `bookstore-realm.json` for realm, client, roles, and test users.

4. **Start Eureka Discovery Server**
   ```bash
   cd eureka-server
   ./mvnw spring-boot:run
   ```
   - Verify at: http://localhost:8761

5. **Start Inventory Microservice**
   ```bash
   cd inventory-ms
   ./mvnw spring-boot:run
   ```

6. **Start User Microservice**
   ```bash
   cd user-ms
   ./mvnw spring-boot:run
   ```

7. **Start Gateway Microservice**
   ```bash
   cd gateway-server
   ./mvnw spring-boot:run
   ```

---

## 📦 Chapter Source Code Availability

The final source code for this chapter is already uploaded in this directory.

Use this folder as the reference implementation for the completed chapter state.

---

## Table of Contents
- [Advanced Security Integration](#advanced-security-integration)
- [Keycloak Identity Management](#keycloak-identity-management)
- [Distributed Tracing with Zipkin](#distributed-tracing-with-zipkin)
- [Production Readiness Best Practices](#production-readiness-best-practices)
- [Testing and Monitoring](#testing-and-monitoring)
- [Summary](#summary)

---

## 🔧 Code Updates Required for This Chapter

This chapter introduces several important code updates to enable advanced security, observability, and production readiness:

### 1. Keycloak Integration
- Add Keycloak configuration to each microservice for OAuth2 and OpenID Connect.
- Update `application.yml` in gateway-server, user-ms, and inventory-ms:
  ```yaml
  spring:
    security:
      oauth2:
        resourceserver:
          jwt:
            issuer-uri: http://localhost:8090/realms/bookstore
            jwk-set-uri: http://localhost:8090/realms/bookstore/protocol/openid-connect/certs
  ```
- Add Keycloak admin client dependency to user-ms:
  ```xml
  <dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>26.0.0</version>
  </dependency>
  ```

### 2. Security Configuration
- Implement role-based access control (RBAC) in gateway-server:
  ```java
  .authorizeExchange(exchange -> exchange
      .pathMatchers(HttpMethod.GET, "/packt/inventory/api/books/**").hasAnyRole("USER", "AUTHOR", "ADMIN")
      .pathMatchers(HttpMethod.POST, "/packt/inventory/api/books").hasAnyRole("AUTHOR", "ADMIN")
      .pathMatchers(HttpMethod.DELETE, "/packt/inventory/api/**").hasRole("ADMIN")
      .anyExchange().authenticated()
  )
  ```
- Extract roles from JWT using a custom converter:
  ```java
  converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      Map<String, Object> realmAccess = jwt.getClaim("realm_access");
      if (realmAccess == null) return Collections.emptyList();
      Collection<String> roles = (Collection<String>) realmAccess.get("roles");
      return roles.stream()
          .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
          .collect(Collectors.toList());
  });
  ```

### 3. Distributed Tracing
- Ensure all microservices are configured to send trace data to Zipkin:
  ```yaml
  spring:
    zipkin:
      base-url: http://localhost:9411
    sleuth:
      sampler:
        probability: 1.0
  ```

### 4. Health Checks & Monitoring
- Enable actuator endpoints in each microservice:
  ```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  ```
- Expose health, info, and metrics endpoints in `application.yml`:
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics
  ```

### 5. Production Readiness
- Review and update logging configuration for trace correlation.
- Ensure proper error handling and fallback mechanisms are implemented.

---

> **Note:** All code updates should be applied to the respective microservice directories (`gateway-server`, `user-ms`, `inventory-ms`, etc.) as described above. Review the source code for each service to ensure the configuration and dependencies match the requirements for this chapter.

---

# Advanced Security Integration

This chapter focuses on:
- Integrating Keycloak for OAuth2 and OpenID Connect
- Enforcing role-based access control (RBAC)
- Propagating user identity across microservices
- Securing endpoints and resources

---

# Keycloak Identity Management

Keycloak provides:
- User management (registration, profile, password reset)
- Authentication (OAuth2, OpenID Connect)
- Authorization (RBAC)
- Token management (JWT issuance, validation, refresh)

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

# Distributed Tracing with Zipkin

Zipkin enables distributed tracing for microservices:
- Run Zipkin with Docker
- View traces at http://localhost:9411
- Integrate Spring Boot microservices with Zipkin for trace propagation

---

# Production Readiness Best Practices

- Centralized authentication and distributed authorization
- Secure configuration management
- Health checks and monitoring endpoints
- Logging and trace correlation
- Automated deployment and rollback

---

# Testing and Monitoring

- Use Postman for API testing
- Monitor service health via Eureka and Zipkin
- Validate security flows with Keycloak

---

# Summary

This chapter demonstrates advanced security, observability, and production readiness for Spring Boot microservices using Keycloak, Zipkin, and best practices for real-world deployment.
