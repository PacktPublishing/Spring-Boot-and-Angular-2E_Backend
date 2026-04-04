# 📘 Chapter 10 — Building and Packaging the Spring Boot Backend

## Chapter Overview

This chapter shifts the Bookstore platform from development-focused implementation to deployment-ready architecture.

So far, we have built a fully functional microservices ecosystem using Spring Boot, Spring Cloud, security, and observability. However, running services locally through IDEs or direct JAR execution is not sufficient for real-world environments.

In this chapter, we focus on how backend services are:

- Packaged into executable artifacts  
- Containerized into portable runtime units  
- Distributed via container registries  
- Executed and managed consistently across environments  
- Orchestrated as a complete platform using Docker Compose  

The goal is simple but critical:

Ensure that the exact same backend system runs consistently across development, testing, and production.

By the end of this chapter, the Bookstore backend becomes a fully portable, reproducible platform.

Source code:  
https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend/tree/main/chapter-10

---

## Table of Contents

- Understanding Backend Packaging Strategies  
- Understanding Containerization Fundamentals  
- Running Ready-Made Containers for the Bookstore Platform  
- Building Custom Container Images for Spring Boot Applications  
- Publishing Container Images to a Container Registry  
- Managing Containers at Runtime  
- Advanced Container Runtime Concepts  
- Orchestrating the Bookstore Platform with Docker Compose  
- One-Command Developer Onboarding Guide  
- Troubleshooting Guide  
- Summary  

---

## Understanding Backend Packaging Strategies

Modern backend development extends beyond writing code into packaging and deployment.

| Approach | Description | Limitation |
|----------|------------|-----------|
| WAR Deployment | External app server required | Heavy |
| Executable JAR | Self-contained | Depends on host |
| Container Image | Fully portable | Requires container runtime |

---

## Understanding Containerization Fundamentals

Containers package OS, runtime, and application into one unit.

| Aspect | VM | Container |
|--------|----|----------|
| OS | Per VM | Shared |
| Startup | Minutes | Seconds |
| Resource usage | Heavy | Lightweight |

---

## Running Ready-Made Containers

```bash
docker pull postgres:16
docker pull mongo:7
docker pull openzipkin/zipkin
```

---

## Building Custom Container Images

```bash
mvn clean package
```

```dockerfile
FROM eclipse-temurin:21-jdk
WORKDIR /opt/bookstore
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

---

## Publishing Container Images

```bash
docker tag image youruser/image
docker login
docker push youruser/image
```

---

## Managing Containers

```bash
docker ps
docker logs container
docker stats
```

---

## Advanced Concepts

- Port mapping  
- Volumes  
- Health checks  
- Networking  

---

## Orchestrating with Docker Compose

```bash
docker compose up -d
```

---

## One-Command Onboarding

```bash
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend.git
cd containerization
docker compose pull
docker compose up -d
```

---

## Troubleshooting Guide

- Check logs  
- Verify ports  
- Validate containers running  

---

## Summary

You now have a containerized, production-ready backend platform.
