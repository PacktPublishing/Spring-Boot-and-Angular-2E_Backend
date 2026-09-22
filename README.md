# Spring Boot and Angular 2E – Backend Repository

This repository is a chapter-by-chapter backend workspace for the Bookstore application in "Spring Boot and Angular – Second Edition". It contains the evolving source code for the same system as it grows from a simple Spring Boot service in Chapter 02 into a full microservices platform by Chapter 10.

Each chapter folder is a progressive stage of the backend. Early chapters focus on the core service and persistence layer, while later chapters add service discovery, API gateway routing, security, observability, SSE notifications, and containerized deployment.

> Start here: [Chapter 02 Environment Setup](chapter-02/ENVIRONMENT_SETUP.md)

---

## Current stack

The code across the later chapters is aligned with the current project setup and uses:

- Java 26
- Spring Boot 4.x
- Spring Cloud 2025.1.2
- Maven 3.9+
- PostgreSQL for inventory data
- MongoDB for user data
- Eureka for service discovery
- Spring Cloud Gateway for routing
- Keycloak for authentication and authorization
- Zipkin for distributed tracing
- Docker and Docker Compose for local infrastructure and runtime orchestration

This repository is not a single final monolith; it contains multiple chapter stages of the same backend architecture.

---

## Quick start

1. Install the required tools by following the environment guide:
   - [Chapter 02 – Environment Setup](chapter-02/ENVIRONMENT_SETUP.md)
2. Make sure Docker is running for PostgreSQL, MongoDB, Zipkin, and Keycloak.
3. Start the services in chapter order, beginning with the discovery server and core microservices.
4. Use the chapter-specific README files for the detailed implementation and test flow for each stage.

---

## Chapter structure

- [chapter-02](chapter-02/) – Baseline microservices setup and environment prep
  - [Environment Setup](chapter-02/ENVIRONMENT_SETUP.md)
  - [Chapter README](chapter-02/README.md)
- [chapter-03](chapter-03/) – AI-assisted development workflow
  - [Chapter README](chapter-03/README.md)
- [chapter-04](chapter-04/) – Database setup with Spring Data JPA and MongoDB
  - [Chapter README](chapter-04/README.md)
- [chapter-05](chapter-05/) – Services and REST APIs
  - [Chapter README](chapter-05/README.md)
- [chapter-06](chapter-06/) – Discovery server and API gateway
  - [Chapter README](chapter-06/README.md)
- [chapter-07](chapter-07/) – OpenAPI docs and observability
  - [Chapter README](chapter-07/README.md)
- [chapter-08](chapter-08/) – Security, OAuth2, JWT, and Keycloak
  - [Chapter README](chapter-08/README.md)
- [chapter-09](chapter-09/) – Reactive programming with Spring WebFlux and SSE
  - [Chapter README](chapter-09/README.md)
- [chapter-10](chapter-10/) – Packaging and containerization
  - [Chapter README](chapter-10/README.md)

---

## Supporting files and tooling

- [containerization/README.md](containerization/README.md) – Docker Compose and runtime stack guide
- [containerization/docker-compose.yml](containerization/docker-compose.yml) – Full backend stack definition
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) – Common runtime and setup issues
- [Postman-Collection](Postman-Collection/) – API collection for exercising the backend

---

## Important notes

- This repository is the backend-only portion of the full Bookstore application.
- The frontend project lives in the separate Angular repository.
- Some project files and chapter folders include generated targets and local build outputs; the source code and chapter READMEs are the best reference when working through the book.

---

## Related repositories

- [Spring Boot and Angular 2E Main Repository](https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E)
- [Spring Boot and Angular 2E Frontend Repository](https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Frontend)

---

## Recommended reading order

1. [Chapter 02 Environment Setup](chapter-02/ENVIRONMENT_SETUP.md)
2. [Chapter 02 README](chapter-02/README.md)
3. [Chapter 03 README](chapter-03/README.md)
4. [Chapter 04 README](chapter-04/README.md)
5. [Chapter 05 README](chapter-05/README.md)
6. [Chapter 06 README](chapter-06/README.md)
7. [Chapter 07 README](chapter-07/README.md)
8. [Chapter 08 README](chapter-08/README.md)
9. [Chapter 09 README](chapter-09/README.md)
10. [Chapter 10 README](chapter-10/README.md)
