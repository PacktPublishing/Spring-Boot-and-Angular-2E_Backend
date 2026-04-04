# 📘 Chapter 10 — Building and Packaging the Spring Boot Backend

## Chapter Overview

This chapter shifts the Bookstore platform from development-focused implementation to deployment-ready architecture.

So far, we have built a complete microservices ecosystem using Spring Boot, Spring Cloud, security, and observability. Now we focus on packaging, containerization, and orchestration to ensure consistency across environments.

By the end of this chapter, the backend becomes a portable, reproducible, production-ready platform.

🔗 Source code:
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

Modern backend development extends beyond code into packaging and deployment.

| Approach | Strength | Limitation |
|----------|--------|-----------|
| WAR | Central runtime | Heavy |
| JAR | Self-contained | Host dependent |
| Container | Fully portable | Needs runtime |

---

## Understanding Containerization Fundamentals

Containers ensure consistency across environments.

| Aspect | VM | Container |
|--------|----|----------|
| OS | Per VM | Shared |
| Startup | Slow | Fast |
| Resources | Heavy | Lightweight |

---

## Running Ready-Made Containers

```bash
docker pull postgres:16
docker pull mongo:7
docker pull openzipkin/zipkin
```

---

## Building Container Images

```bash
mvn clean package
docker build -t bookstore/inventory-service:v0.0.1 .
```

---

## Publishing Images

```bash
docker tag image youruser/image
docker push youruser/image
```

---

## Managing Containers

```bash
docker ps
docker logs <container>
docker stats
```

---

## Docker Compose

```bash
docker compose up -d
```

---

## One-Command Onboarding

```bash
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend.git
cd containerization
docker compose up -d
```

---

## Troubleshooting

- Check logs: docker logs
- Verify ports
- Ensure services are running

---

## Summary

You now have a containerized, portable backend ready for deployment.
