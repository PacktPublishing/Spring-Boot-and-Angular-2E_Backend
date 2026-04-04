# Chapter 10 - Building and Packaging the Spring Boot Backend

## Chapter Overview

In the previous chapter, we introduced real-time updates with Spring WebFlux and Server-Sent Events. In this chapter, the focus shifts from feature development to deployment readiness.

Until now, the backend services mostly ran from IDE sessions or direct JAR execution. That approach is useful for development, but modern environments require consistent packaging, portability, and repeatable runtime behavior.

This chapter covers how to:

- Package Spring Boot services for execution
- Containerize backend services and infrastructure
- Build and publish container images to a registry
- Operate and troubleshoot containers at runtime
- Orchestrate the complete backend stack with Docker Compose

By the end of this chapter, the Bookstore backend can run as a portable and reproducible platform across developer machines, CI pipelines, and production-like environments.

Technical requirement and source code:
https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend/tree/main/chapter-10

Docker Compose user guide for running the full backend using author-published images:
[containerization/README.md](../containerization/README.md)

## Table of Contents

- [Understanding Backend Packaging Strategies](#understanding-backend-packaging-strategies)
- [Understanding Containerization Fundamentals](#understanding-containerization-fundamentals)
- [Running Ready-Made Containers for the Bookstore Platform](#running-ready-made-containers-for-the-bookstore-platform)
- [Building Custom Container Images for Spring Boot Applications](#building-custom-container-images-for-spring-boot-applications)
- [Publishing Container Images to a Container Registry](#publishing-container-images-to-a-container-registry)
- [Managing Containers at Runtime](#managing-containers-at-runtime)
- [Common Pitfalls When Containerizing Spring Boot Applications](#common-pitfalls-when-containerizing-spring-boot-applications)
- [Orchestrating the Bookstore Platform with Docker Compose](#orchestrating-the-bookstore-platform-with-docker-compose)
- [One-Command Developer Onboarding Guide](#one-command-developer-onboarding-guide)
- [Docker Compose Companion Guide](#docker-compose-companion-guide)
- [Troubleshooting Guide](#troubleshooting-guide)
- [References](#references)
- [Summary](#summary)

## Understanding Backend Packaging Strategies

Packaging is not just a build-step detail. It directly affects deployment speed, runtime consistency, and operational complexity.

For Java backends, three packaging models are common:

| Approach | Description | Typical Limitation |
|---|---|---|
| WAR deployment | App packaged and deployed to an external app server | Operational coupling to app-server setup and version |
| Executable JAR | Self-contained Spring Boot artifact with embedded server | Depends on host runtime setup |
| Container image | App + runtime + dependencies packaged as one immutable artifact | Requires container runtime |

Spring Boot executable JARs simplified deployment significantly, but container images go one step further by packaging the runtime environment itself. That portability is why container-based packaging has become the default for microservices.

## Understanding Containerization Fundamentals

Containers and virtual machines both isolate workloads, but their architecture differs substantially:

- Virtual machines include a full guest OS and kernel
- Containers share the host kernel and isolate processes with namespaces and cgroups
- Containers start faster and consume fewer resources

Core concepts used in this chapter:

1. Container image layers: each Dockerfile or Containerfile instruction creates a cached read-only layer.
2. Container runtime: Docker, Podman, or lower-level runtimes execute and manage containers.
3. Lifecycle flow: build image, store in registry, pull image, run container, monitor and manage runtime state.

This model eliminates environment drift and supports repeatable deployments for the Bookstore platform.

## Running Ready-Made Containers for the Bookstore Platform

Before containerizing custom Spring Boot services, supporting infrastructure can run from official images.

Main services used in this chapter:

- PostgreSQL for inventory data
- MongoDB for user data
- Zipkin for distributed tracing
- Keycloak for identity and access management

Example commands:

```bash
docker pull postgres:16
docker run -d --name bookstore-postgres \
  -e POSTGRES_DB=inventorydb \
  -e POSTGRES_USER=inventoryuser \
  -e POSTGRES_PASSWORD=inventorypass \
  -p 5432:5432 postgres:16

docker pull mongo:7
docker run -d --name bookstore-mongo -p 27017:27017 mongo:7

docker pull openzipkin/zipkin
docker run -d --name bookstore-zipkin -p 9411:9411 openzipkin/zipkin
```

Once running, use:

```bash
docker ps
docker logs bookstore-postgres
docker stop bookstore-postgres
docker rm bookstore-postgres
```

## Building Custom Container Images for Spring Boot Applications

The chapter shows three image build approaches for Spring Boot services.

1. Manual Dockerfile or Containerfile
2. Spring Boot Buildpacks (`mvn spring-boot:build-image`)
3. Google Jib (`mvn compile jib:dockerBuild`)

A common production-ready pattern is the multi-stage build:

```dockerfile
# Stage 1: build
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/inventory-ms-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

This approach keeps runtime images smaller and removes build toolchain dependencies from production containers.

## Publishing Container Images to a Container Registry

After building locally, images should be tagged and pushed so other environments can pull the exact same artifact.

Typical workflow:

```bash
docker tag bookstore/inventory-service:v0.0.1 ansgohar/inventory-service:v0.0.1
docker login
docker push ansgohar/inventory-service:v0.0.1
docker pull ansgohar/inventory-service:v0.0.1
```

Using explicit version tags improves rollback safety and deployment reproducibility.

## Managing Containers at Runtime

Day-to-day operations involve starting, observing, and diagnosing running containers.

Common runtime commands:

- `docker run -d ...` for detached mode
- `docker ps` and `docker ps -a` for state inspection
- `docker logs -f <container>` for live logs
- `docker stats` for CPU and memory usage
- `docker inspect <container>` for detailed configuration

Useful runtime behaviors in production-like environments:

- Restart policies, for example `--restart=always`
- Health checks to detect unhealthy service state
- Persistent volumes for stateful services
- User-defined bridge networks for service-to-service DNS

## Common Pitfalls When Containerizing Spring Boot Applications

Frequent issues and practical safeguards:

1. Image too large: use multi-stage builds and JRE runtime images.
2. Slow rebuilds: isolate dependency download layers before copying source code.
3. Startup failures due to dependency timing: use health checks and startup dependencies.
4. Misconfigured service URLs: inside containers, use service names instead of `localhost`.
5. Data loss in local databases: attach named volumes for PostgreSQL and MongoDB.
6. Unpredictable deployments with floating tags: prefer explicit version tags.

## Orchestrating the Bookstore Platform with Docker Compose

Running every service manually does not scale. Docker Compose defines the complete system in one declarative file so the stack can be started and stopped consistently.

Compose models the platform through:

- Services (application and infrastructure containers)
- Networks (service discovery and communication)
- Volumes (persistent data)
- Dependencies and health checks (startup ordering)

In this repository, compose orchestration is provided in the shared file:

- `containerization/docker-compose.yml`

The runtime architecture includes:

- API Gateway as the single entry point
- Eureka for discovery
- Inventory and User services for domain APIs
- PostgreSQL and MongoDB for persistence
- Zipkin for tracing
- Keycloak for authentication and authorization

## One-Command Developer Onboarding Guide

Use this quick onboarding flow to run the full backend stack.

```bash
# 1) Clone and enter the repository
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend.git
cd Spring-Boot-and-Angular-2E_Backend

# 2) Move to compose folder
cd containerization

# 3) Pull and start all services
docker compose pull
docker compose up -d

# 4) Verify status
docker compose ps
```

Key endpoints:

- Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- Zipkin: http://localhost:9411
- Keycloak: http://localhost:8090

## Docker Compose Companion Guide

A detailed compose runbook (including build options, service checks, common commands, and extended troubleshooting) is available at:

- [Containerization Guide](../containerization/README.md)

## Troubleshooting Guide

Start with the companion guide for detailed docker-compose troubleshooting:

- [Containerization Guide Troubleshooting](../containerization/README.md#troubleshooting)

Quick checks from this chapter:

1. Verify Docker is running and has enough memory/disk.
2. Confirm required ports are free: `5432`, `27017`, `8090`, `8761`, `8080`, `9411`.
3. Confirm images are accessible: `docker compose pull`.
4. Check service health and logs:

```bash
docker compose ps
docker compose logs -f --tail=200
```

5. If startup order issues occur, restart only failed services after dependencies are healthy:

```bash
docker compose up -d eureka-server inventory-service user-service gateway-server
```

## References

- Chapter source code: https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend/tree/main/chapter-10
- Spring Boot container images: https://docs.spring.io/spring-boot/docs/current/reference/html/container-images.html
- Google Jib: https://github.com/GoogleContainerTools/jib
- Docker Compose docs: https://docs.docker.com/compose/

## Summary

This chapter moves the Bookstore backend from local-only execution to a portable containerized architecture.

You learned how to package Spring Boot services, build and publish container images, run and monitor containers, and orchestrate the full backend platform with Docker Compose. Those capabilities form the operational baseline needed before integrating the Angular frontend and preparing production-grade deployments later in the book.
