# Chapter 06 — Service Discovery & API Gateway Using Spring Cloud

This chapter introduces **Service Discovery** and **API Gateway** patterns in a Spring Cloud microservices environment. You'll build a **Eureka Discovery Server**, register your microservices, and implement a **Spring Cloud Gateway** that routes, filters, and protects traffic—all dynamically and without hardcoded URLs.

---

## Table of Contents

1. [Chapter Overview](#chapter-overview)
2. [Understanding Service Discovery](#understanding-service-discovery)
3. [Implementing Spring Cloud Eureka Discovery Server](#implementing-spring-cloud-eureka-discovery-server)
4. [Registering Microservices with Eureka](#registering-microservices-with-eureka)
5. [Introducing the API Gateway Pattern](#introducing-the-api-gateway-pattern)
6. [Gateway Technology Comparison](#gateway-technology-comparison)
7. [Spring Cloud Gateway Architecture](#spring-cloud-gateway-architecture)
8. [Implementing Spring Cloud Gateway](#implementing-spring-cloud-gateway)
9. [Circuit Breaker Support with Resilience4j](#circuit-breaker-support-with-resilience4j)
10. [Installation & Setup Steps](#installation--setup-steps)
11. [Resources & References](#resources--references)

---

## Chapter Overview

This chapter brings together service discovery and API Gateway patterns to create a robust, scalable microservices infrastructure. You'll learn:

- The difference between static and dynamic service discovery
- How to implement Eureka Discovery Server for service registration
- Registering microservices as Eureka clients
- The benefits and architecture of API Gateway pattern
- Comparing different gateway technologies
- Building a Spring Cloud Gateway with routing and filtering
- Implementing circuit breaker patterns with Resilience4j
- Testing and monitoring your distributed system
- Production-ready configuration for discovery and gateway

---

## Understanding Service Discovery

Microservices run in distributed environments where IPs and ports change frequently due to scaling, failures, or redeployments. Static URLs break quickly in such dynamic environments. Dynamic service discovery solves this by allowing services to register themselves and discover others at runtime.

### Why Service Discovery?

- **Dynamic scaling** — Services can scale up/down without manual configuration
- **Fault tolerance** — Failed instances are automatically removed from registry
- **Load balancing** — Distribute requests across healthy instances
- **Location transparency** — Clients don't need to know service locations
- **Zero-downtime deployments** — New instances register before old ones shut down

### Static vs Dynamic Discovery

| Aspect | Static Configuration | Dynamic Discovery |
|--------|---------------------|-------------------|
| Location | Hardcoded IPs/ports | Registry-based lookup |
| Scaling | Manual updates required | Auto-updated on registration |
| Failover | Requires external monitoring | Built-in health checks |
| Deployment | Restart/reconfigure clients | Automatic discovery |
| Best For | Small, stable projects | Cloud-native microservices |

### Discovery Approaches

#### DNS-based Discovery

**Pros:**
- Built into infrastructure
- No additional components needed
- Works with all clients

**Cons:**
- Slow TTL-based updates
- No health checking
- Limited metadata support
- Cache invalidation issues

#### Registry-based Discovery (Eureka)

**Pros:**
- Real-time health checks
- Rich service metadata
- Instant updates on changes
- Client-side load balancing
- Programmatic service discovery

**Cons:**
- Additional infrastructure component
- Learning curve
- Network dependency

For production microservices, **registry-based discovery is essential** for reliability and flexibility.

---

## Implementing Spring Cloud Eureka Discovery Server

Eureka is Netflix's service registry, now part of Spring Cloud. It provides a REST-based service registry for resilient mid-tier load balancing and failover.

### Step 1 — Create Discovery Server Project

Use Spring Initializr (<https://start.spring.io>) with:

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.5.x
- **Dependencies**:
  - Spring Cloud Netflix Eureka Server
  - Spring Boot Actuator

### Step 2 — Add Dependencies

Add to `pom.xml`:

```xml
<properties>
    <spring-cloud.version>2025.0.0</spring-cloud.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

### Step 3 — Enable Eureka Server

```java
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
```

### Step 4 — Configure application.yml

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-server

eureka:
  instance:
    hostname: localhost
  
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 60000

logging:
  level:
    com.netflix.eureka: INFO
    com.netflix.discovery: INFO
```

### Step 5 — Start Eureka Server

```bash
cd discovery-server
./mvnw spring-boot:run
```

Visit **http://localhost:8761** to see the Eureka dashboard.

---

## Registering Microservices with Eureka

Now that Eureka Server is running, let's register our Inventory and User microservices.

### Step 1 — Add Eureka Client Dependency

Add to each microservice `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Step 2 — Configure Inventory Service

Update `inventory-service/application.yml`:

```yaml
server:
  port: 8081
  servlet:
    context-path: /inventory

spring:
  application:
    name: inventory-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

### Step 3 — Configure User Service

Update `user-service/application.yml`:

```yaml
server:
  port: 8082
  servlet:
    context-path: /user

spring:
  application:
    name: user-ms

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

### Step 4 — Start All Services

```bash
# Terminal 1 - Discovery Server
cd discovery-server
./mvnw spring-boot:run

# Terminal 2 - Inventory Service
cd inventory-service
./mvnw spring-boot:run

# Terminal 3 - User Service
cd user-service
./mvnw spring-boot:run
```

Check the Eureka dashboard at **http://localhost:8761** — both services should appear as registered instances.

---

## Introducing the API Gateway Pattern

An API Gateway acts as a **reverse proxy** and **single entry point** for all client requests, routing them to appropriate microservices.

### Key Benefits

| Benefit | Description | Impact |
|---------|-------------|--------|
| **Centralized routing** | One public URL for all services | Simplified client configuration |
| **Unified security** | Single place for authentication/authorization | Consistent security policies |
| **Protocol translation** | Convert between HTTP/gRPC/WebSocket | Client flexibility |
| **API versioning** | Route v1/v2 traffic seamlessly | Backward compatibility |
| **Rate limiting** | Throttle requests per client | Prevent abuse |
| **Request aggregation** | Combine multiple service calls | Reduced network overhead |
| **Caching** | Cache responses at gateway level | Improved performance |
| **Monitoring** | Centralized logging and metrics | Better observability |

### Gateway Use Cases

- **Mobile backends** — Aggregate multiple service calls into one
- **Third-party APIs** — Control external access with rate limiting
- **Legacy integration** — Translate protocols and formats
- **Canary deployments** — Route small percentage to new version
- **A/B testing** — Split traffic based on user attributes

---

## Gateway Technology Comparison

| Gateway | Type | Pros | Cons | Best For |
|---------|------|------|------|----------|
| **Spring Cloud Gateway** | Java, Reactive | Spring ecosystem integration, Eureka support | Smaller plugin ecosystem | Spring microservices |
| **Kong** | Nginx-based | Fast, extensive plugins, OpenAPI support | External infrastructure, Lua config | Polyglot services |
| **Apigee** | Enterprise SaaS | Advanced analytics, monetization, developer portal | Expensive, vendor lock-in | Large enterprises |
| **AWS API Gateway** | Managed service | Serverless, AWS integration, auto-scaling | AWS lock-in, cost at scale | AWS-native apps |
| **Traefik** | Go-based | Auto-discovery, Docker/K8s native, modern | Less mature for complex routing | Container environments |
| **Netflix Zuul** | Java | Battle-tested | Deprecated, blocking I/O | Legacy projects only |

**We choose Spring Cloud Gateway** for:
- Seamless Spring Boot integration
- Reactive non-blocking architecture
- Built-in Eureka support
- Programmatic route configuration
- Native circuit breaker integration

---

## Spring Cloud Gateway Architecture

Spring Cloud Gateway is built on Spring WebFlux and uses a reactive, non-blocking architecture.

### Request Flow

1. **Client Request** → Gateway receives incoming HTTP request
2. **Predicate Matching** → Evaluates route predicates (path, headers, query params)
3. **Route Selection** → Selects matching route definition
4. **Pre-filters** → Apply filters before proxying (auth, rate limiting, header modification)
5. **Service Discovery** → Resolve service instance via Eureka (`lb://service-name`)
6. **Load Balancing** → Select healthy instance using client-side load balancer
7. **Proxy Request** → Forward request to target microservice
8. **Post-filters** → Apply filters to response (logging, header modification)
9. **Response** → Return to client

### Core Components

**Route** — Defines destination URI, predicates, and filters

```java
Route.async(r -> r
    .id("inventory-route")
    .uri("lb://inventory-service")
    .predicate(path("/inventory/**"))
    .filter(rewritePath("/inventory/(?<segment>.*)", "/${segment}"))
)
```

**Predicate** — Condition that must be true for route to match

- Path matching: `/api/books/**`
- Header matching: `X-Request-Type=mobile`
- Query parameter: `?version=2`
- Time-based: After/Before specific time
- Method matching: GET, POST, PUT, DELETE

**Filter** — Modifies request or response

- AddRequestHeader
- RemoveResponseHeader
- RewritePath
- CircuitBreaker
- RateLimiter
- RequestRateLimiter

---

## Implementing Spring Cloud Gateway

### Step 1 — Create Gateway Project

Use Spring Initializr with:

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.5.x
- **Dependencies**:
  - Spring Cloud Gateway
  - Eureka Discovery Client
  - Spring Boot Actuator

### Step 2 — Add Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

### Step 3 — Configure application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway-server

  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      
      routes:
        # Inventory Service Route
        - id: inventory-route
          uri: lb://inventory-service
          predicates:
            - Path=/packt/inventory/api/**
          filters:
            - RewritePath=/packt/inventory/api/(?<segment>.*), /inventory/api/${segment}
            - AddRequestHeader=X-Gateway, BookstoreGateway
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20

        # User Service Route
        - id: user-route
          uri: lb://user-ms
          predicates:
            - Path=/packt/user/api/**
          filters:
            - RewritePath=/packt/user/api/(?<segment>.*), /user/api/${segment}
            - AddRequestHeader=X-Gateway, BookstoreGateway

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true

management:
  endpoints:
    web:
      exposure:
        include: gateway,health,info,metrics
  endpoint:
    gateway:
      enabled: true

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO
```

### Step 4 — Enable Gateway

```java
@SpringBootApplication
public class GatewayServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GatewayServerApplication.class, args);
    }
}
```

### Step 5 — Custom Route Configuration (Optional)

For programmatic route configuration:

```java
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("inventory-route", r -> r
                .path("/packt/inventory/api/**")
                .filters(f -> f
                    .rewritePath("/packt/inventory/api/(?<segment>.*)", "/inventory/api/${segment}")
                    .addRequestHeader("X-Gateway", "BookstoreGateway")
                    .addResponseHeader("X-Response-Time", String.valueOf(System.currentTimeMillis()))
                )
                .uri("lb://inventory-service")
            )
            .route("user-route", r -> r
                .path("/packt/user/api/**")
                .filters(f -> f
                    .rewritePath("/packt/user/api/(?<segment>.*)", "/user/api/${segment}")
                    .addRequestHeader("X-Gateway", "BookstoreGateway")
                )
                .uri("lb://user-ms")
            )
            .build();
    }
}
```

### Step 6 — Test Gateway Routing

```bash
# Through Gateway (port 8080)
curl http://localhost:8080/packt/inventory/api/books

# Direct to service (port 8081)
curl http://localhost:8081/inventory/api/books
```

---

## Circuit Breaker Support with Resilience4j

Circuit breakers prevent cascading failures by detecting service failures and providing fallback responses.

### Step 1 — Add Resilience4j Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```

### Step 2 — Configure Circuit Breaker

Add to `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: inventory-route-with-cb
          uri: lb://inventory-service
          predicates:
            - Path=/packt/inventory/api/**
          filters:
            - RewritePath=/packt/inventory/api/(?<segment>.*), /inventory/api/${segment}
            - name: CircuitBreaker
              args:
                name: inventoryCB
                fallbackUri: forward:/fallback/inventory

resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
    instances:
      inventoryCB:
        base-config: default
```

### Step 3 — Create Fallback Controller

```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventoryFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Inventory service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response);
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "User service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response);
    }
}
```

### Step 4 — Test Circuit Breaker

```bash
# Stop inventory service to trigger circuit breaker
# Make requests through gateway
curl http://localhost:8080/packt/inventory/api/books

# Should return fallback response after threshold is reached
```

### Circuit Breaker States

| State | Description | Behavior |
|-------|-------------|----------|
| **CLOSED** | Normal operation | All requests go through |
| **OPEN** | Service failing | All requests return fallback immediately |
| **HALF_OPEN** | Testing recovery | Limited requests to test if service recovered |

---

## Installation & Setup Steps

### 1. Start Eureka Discovery Server

```bash
cd discovery-server
./mvnw spring-boot:run
```

Verify at: **http://localhost:8761**

### 2. Start Microservices

```bash
# Terminal 1 - Inventory Service
cd inventory-service
./mvnw spring-boot:run

# Terminal 2 - User Service
cd user-service
./mvnw spring-boot:run
```

### 3. Start API Gateway

```bash
cd gateway-server
./mvnw spring-boot:run
```

### 4. Verify Registration

Check Eureka dashboard — all three services should be registered:
- `GATEWAY-SERVER`
- `INVENTORY-SERVICE`
- `USER-MS`

### 5. Test API Gateway

```bash
# Get all books through gateway
curl http://localhost:8080/packt/inventory/api/books

# Get users through gateway
curl http://localhost:8080/packt/user/api/users

# Check gateway routes
curl http://localhost:8080/actuator/gateway/routes
```

### 6. Test Service Discovery

```bash
# Scale inventory service to multiple instances
cd inventory-service
./mvnw spring-boot:run -Dserver.port=8091

# Gateway will load balance between 8081 and 8091
```

---

## Resources & References

- **Spring Cloud Gateway Documentation**: <https://spring.io/projects/spring-cloud-gateway>
- **Spring Cloud Netflix Documentation**: <https://spring.io/projects/spring-cloud-netflix>
- **Resilience4j Documentation**: <https://resilience4j.readme.io/>
- **Eureka Wiki**: <https://github.com/Netflix/eureka/wiki>
- **API Gateway Pattern**: <https://microservices.io/patterns/apigateway.html>


---