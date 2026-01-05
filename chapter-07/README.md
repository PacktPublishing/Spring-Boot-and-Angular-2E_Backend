# 📘 Chapter 07 — Documenting APIs & Enabling Application Observability Using Spring Boot

## Chapter Overview
This chapter builds on the resilient communication layer introduced in Chapter 06 by making our microservices **understandable** and **observable**.  
You will document REST APIs using **OpenAPI (Swagger)** and enable **logging, metrics, and distributed tracing** so that every request in the Bookstore system can be understood, monitored, and debugged with confidence.

---

## Table of Contents
- [Understanding Documentation & Observability](#understanding-documentation--observability)
- [Documenting APIs with OpenAPI](#documenting-apis-with-openapi)
- [Implementing OpenAPI with Springdoc](#implementing-openapi-with-springdoc)
- [OpenAPI Annotations in Practice](#openapi-annotations-in-practice)
- [API Documentation Best Practices](#api-documentation-best-practices)
- [Observability Fundamentals](#observability-fundamentals)
- [Logging with SLF4J & Logback](#logging-with-slf4j--logback)
- [Distributed Tracing with Micrometer & Zipkin](#distributed-tracing-with-micrometer--zipkin)
- [Observability Best Practices & Pitfalls](#observability-best-practices--pitfalls)
- [Summary](#summary)  

---

# Understanding Documentation & Observability

In microservices architectures, **working APIs are not enough**.  
They must be:
- Discoverable and easy to consume (documentation)
- Transparent and traceable in production (observability)

Documentation tells consumers **what should happen**.  
Observability tells operators **what actually happened**.

Together, they form the foundation of operational excellence.

---

# Documenting APIs with OpenAPI

OpenAPI is the industry standard for describing REST APIs.

It defines:
| Feature | Description |
|-------|-------------|
| Paths | Available endpoints |
| Methods | HTTP verbs |
| Parameters | Path, query, headers |
| Schemas | Request/response models |
| Errors | Failure scenarios |
| Security | Authentication methods |

In Spring Boot 3+, the recommended tool is **springdoc-openapi**.

---

# Implementing OpenAPI with Springdoc

## Step 1 — Add Dependency
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.3.0</version>
</dependency>
```

## Step 2 — Access Documentation
```
/v3/api-docs
/swagger-ui.html
```

Example:
```
http://localhost:8081/inventory/swagger-ui.html
```

---

# OpenAPI Annotations in Practice

## @Operation
```java
@Operation(
  summary = "Retrieve all books",
  description = "Returns a paginated and sortable list of books"
)
```

## @ApiResponses
```java
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Success"),
  @ApiResponse(responseCode = "400", description = "Invalid request"),
  @ApiResponse(responseCode = "500", description = "Server error")
})
```

## @Parameter
```java
@Parameter(
  name = "page",
  description = "Page number (0-based)",
  example = "0"
)
```

## @Tag
```java
@Tag(
  name = "Book Inventory",
  description = "Inventory management APIs"
)
```

These annotations make APIs **self-describing**, **interactive**, and **tool-friendly**.

---

# API Documentation Best Practices

| Practice | Benefit |
|--------|---------|
| Use @Operation & @ApiResponse | Clear intent |
| Document error responses | Safer clients |
| Version endpoints | Backward compatibility |
| Group endpoints with @Tag | Better UX |
| Keep docs near code | Prevent drift |

---

# Observability Fundamentals

Observability answers the question:
**Can we understand system behavior from its outputs?**

It consists of three pillars:

| Pillar | Purpose |
|-------|---------|
| Logs | Discrete events |
| Metrics | Aggregated measurements |
| Traces | End-to-end request flow |

---

# Logging with SLF4J & Logback

Spring Boot uses SLF4J with Logback by default.

```java
log.info("Fetching book with id {}", id);
```

## Correlated Logging
```yaml
logging:
  pattern:
    correlation: "[${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

This enriches every log entry with trace context.

---

# Distributed Tracing with Micrometer & Zipkin

## Dependencies
```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<dependency>
  <groupId>io.zipkin.reporter2</groupId>
  <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

## Configuration
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

## Run Zipkin
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

UI:
```
http://localhost:9411
```

---

# Observability Best Practices & Pitfalls

## Common Pitfalls
| Issue | Impact |
|-----|--------|
| Over-logging | Noise & performance hit |
| Missing trace IDs | Hard debugging |
| Logging secrets | Security risk |
| Only infra metrics | No business insight |

## Best Practices
| Area | Recommendation |
|-----|----------------|
| Logs | Consistent log levels |
| Traces | Always propagate traceId |
| Metrics | Track domain KPIs |
| Security | Lock down actuator & Swagger |

---

# Summary

In this chapter, you implemented:

✔ OpenAPI documentation with Springdoc  
✔ Interactive Swagger UI for all services  
✔ Structured, correlated logging  
✔ Distributed tracing with Micrometer & Zipkin  
✔ Production-grade observability foundations  

Your Bookstore microservices are now **documented, observable, and diagnosable**.  
In the next chapter, we will secure these APIs and observability endpoints using **Spring Security and Keycloak**.

---

# Resources & References

## Official Documentation
- [OpenAPI Specification](https://spec.openapis.org/oas/v3.1.0) — Complete OpenAPI 3.1.0 specification
- [Springdoc OpenAPI](https://springdoc.org/) — Official Springdoc documentation
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/) — Production-ready features guide
- [Micrometer Tracing](https://micrometer.io/docs/tracing) — Micrometer distributed tracing documentation

## Useful Tools & Services
- [Swagger Editor](https://editor.swagger.io/) — Interactive OpenAPI editor
- [Zipkin](https://zipkin.io/) — Distributed tracing system
- [Spring Boot Actuator Endpoints](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) — Complete endpoint reference