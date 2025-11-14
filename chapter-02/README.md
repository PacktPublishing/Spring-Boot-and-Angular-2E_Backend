# Chapter 02 — Getting Started With Microservices Using Spring Boot

This chapter introduces the fundamental concepts of microservices architecture and walks you through setting up your development environment, understanding essential design patterns, and creating your first Spring Boot microservice.

---

## Table of Contents

1. [Chapter Overview](#chapter-overview)
2. [Introduction to Microservices](#introduction-to-microservices)
3. [Microservice Design Patterns](#microservice-design-patterns)
4. [Microservice Best Practices](#microservice-best-practices)
5. [Bookstore Architecture Overview](#bookstore-architecture-overview)
6. [Development Environment Setup](#development-environment-setup)
7. [Creating Your First Spring Boot Microservice](#creating-your-first-spring-boot-microservice)
8. [Installation & Setup Steps](#installation--setup-steps)
9. [Resources & References](#resources--references)

---

## Chapter Overview

This chapter provides a foundation for understanding microservice architecture, essential patterns, and best practices. You'll learn:

- Core microservice characteristics and principles
- Essential design patterns for building scalable systems
- Best practices for development, operations, and team collaboration
- The architecture of the Bookstore application
- How to set up your development environment
- How to create and structure your first Spring Boot microservice

---

## Introduction to Microservices

Microservices are small, autonomous services that encapsulate single business capabilities. They communicate through lightweight protocols, typically REST or messaging, and own their data stores.

### Key Characteristics

- **Loose coupling** — Services are independent and can be deployed separately
- **High cohesion** — Each service focuses on a single business capability
- **Technology-agnostic** — Teams can choose the best tools for each service
- **Decentralized governance** — Teams own their services end-to-end
- **Built-in fault tolerance** — Services handle failures gracefully

---

## Microservice Design Patterns

### Decomposition Patterns

- **Decompose by business capability** — Organize services around business functions
- **Decompose by subdomain** — Align services with domain-driven design subdomains

### Integration Patterns

- **API Gateway** — Single entry point for all client requests
- **Backend for Frontend (BFF)** — Separate backends for different client types
- **Service Mesh** — Infrastructure layer for service-to-service communication
- **Message-based communication** — Asynchronous communication via message brokers

### Data Management Patterns

- **Database per service** — Each service owns its data store
- **Event sourcing** — Store state changes as sequence of events
- **CQRS** — Separate read and write models
- **Saga Pattern** — Manage distributed transactions across services

### Reliability Patterns

- **Circuit breaker** — Prevent cascading failures
- **Retry with backoff** — Retry failed requests with increasing delays
- **Bulkhead isolation** — Isolate resources to prevent system-wide failures
- **Timeouts and fallbacks** — Set timeouts and provide fallback responses

### Observability Patterns

- **Centralized logging** — Aggregate logs from all services
- **Distributed tracing** — Track requests across service boundaries
- **Metrics collection** — Monitor service health and performance
- **Health checks** — Expose service health status

---

## Microservice Best Practices

### Development Process Best Practices

- Maintain layered architecture (Controller → Service → Repository → DTO/Mapper)
- Enforce clean architecture boundaries
- Use DTOs to decouple internal domain models
- Implement comprehensive testing (unit, integration, contract)
- Keep services small, cohesive, and domain-aligned

### Operation Excellence & DevOps Best Practices

- Containerize all services using Docker
- Automate CI/CD pipelines
- Implement API versioning
- Use structured JSON logging
- Enable health and metrics endpoints via Spring Boot Actuator

### Team Collaboration Best Practices

- Organize teams around business domains
- Establish strong service ownership
- Document APIs using Swagger/OpenAPI
- Use developer portals & service catalogs
- Maintain lightweight governance for consistency

---

## Bookstore Architecture Overview

The Bookstore application is built on modern cloud-native principles using:

- **Spring Boot Microservices** — Inventory + User Management services
- **Spring Cloud Gateway** — API Gateway for routing and load balancing
- **Keycloak** — Authentication & JWT validation
- **PostgreSQL & MongoDB** — Polyglot persistence for different data needs
- **Prometheus & Grafana** — Observability and monitoring
- **Angular 20** — Modern frontend framework

Each microservice follows a clean layered architecture with clear separation of concerns.

---

## Development Environment Setup

### Required Tools

| Tool | Version | Purpose |
|------|---------|----------|
| Java JDK | 21+ (25) | Compiling & running Spring Boot services |
| Maven | Latest | Build automation |
| Git | Any | Version control |
| VS Code | Latest | IDE for Spring & Angular |
| Docker | Latest | Containerization |

### Recommended VS Code Extensions

- **Extension Pack for Java** — Comprehensive Java development support
- **Spring Boot Extension Pack** — Spring Boot development tools
- **Docker Extension** — Container management
- **REST Client** — Test REST endpoints

---

## Creating Your First Spring Boot Microservice

### Step 1: Set Up VS Code

Install the required extensions:

```
Ctrl+Shift+X → Search "Extension Pack for Java"
Ctrl+Shift+X → Search "Spring Boot Extension Pack"
```

### Step 2: Generate the Microservice

Use **Spring Initializr** (<https://start.spring.io>) with:

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.5.x
- **Dependencies**: Spring Web, Spring Boot DevTools

Download and extract the generated project.

### Step 3: Run the Application

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`

### Step 4: Apply Layered Architecture

Organize your code with this recommended structure:

```
src/main/java/com/bookstore/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── dto/            # Data transfer objects
├── model/          # Domain entities
└── config/         # Configuration classes
```

### Example Code Snippets

**Model (Entity)**

```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String author;
    private BigDecimal price;
    
    // Getters and setters
}
```

**Repository**

```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthor(String author);
    List<Book> findByTitleContainingIgnoreCase(String title);
}
```

**Service**

```java
@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }
}
```

**Controller**

```java
@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookService bookService;
    
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}
```

---

## Installation & Setup Steps

### 1. Install Java JDK 24

Download from: <https://www.oracle.com/java/technologies/downloads/>

Verify installation:
```bash
java -version
```

### 2. Install Maven

Download from: <https://maven.apache.org/download.cgi>

Verify installation:
```bash
mvn -version
```

### 3. Install VS Code

Download from: <https://code.visualstudio.com>

### 4. Install Docker

Download from: <https://www.docker.com/products/docker-desktop>

### 5. Clone the Bookstore Repositories

```bash
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Frontend
```

---

## Resources & References

- **Spring Boot Documentation**: <https://spring.io/projects/spring-boot>
- **Spring Initializr**: <https://start.spring.io>
- **Microservices.io**: <https://microservices.io>
- **Spring Cloud Documentation**: <https://spring.io/projects/spring-cloud>
- **VS Code Documentation**: <https://code.visualstudio.com/docs>

---