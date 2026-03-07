# Bookstore Microservices - Docker Containerization Guide

## Overview

This guide provides step-by-step instructions to containerize, build, push to Docker Hub, and deploy the Bookstore microservices using Docker and Docker Compose. The architecture includes:

- **Eureka Server**: Service registry and discovery
- **API Gateway**: Single entry point for all client requests
- **Inventory Service**: Manages inventory data (PostgreSQL)
- **User Service**: Manages user data (MongoDB)
- **PostgreSQL**: Database for inventory service
- **MongoDB**: Database for user service
- **Zipkin**: Distributed tracing for observability

## Architecture & Startup Order

The docker-compose.yml enforces the following startup order through health checks and dependencies:

1. **PostgreSQL** - Database initialization
2. **MongoDB** - NoSQL database initialization
3. **Zipkin** - Observability platform
4. **Eureka Server** - Service registry (waits for Zipkin)
5. **Inventory Service** - Microservice (waits for PostgreSQL & Eureka)
6. **User Service** - Microservice (waits for MongoDB & Eureka)
7. **API Gateway** - Entry point (waits for all microservices)

## Prerequisites

- Docker (v20.10+)
- Docker Compose (v2.0+)
- Docker Hub account (for pushing images)
- Maven 3.9+ (if building locally)
- Java 21+ (if building locally)

## Quick Start

### Option 1: Using Pre-built Images from Docker Hub

If you've already pushed images to Docker Hub, you can run:

```bash
docker-compose up -d
```

### Option 2: Build and Run Locally

Navigate to the chapter-19 directory:

```bash
cd /Users/ahmadgohar/GitHub/Packt/Spring-Boot-and-Angular-2E_Backend/chapter-19
```

Build all images and start services:

```bash
docker-compose up --build
```

## Building and Pushing to Docker Hub

### Using the Automated Build Script (Recommended)

The easiest way to build and push images to Docker Hub is using the provided automation script:

```bash
# Navigate to the project root
cd /Users/ahmadgohar/GitHub/Packt/Spring-Boot-and-Angular-2E_Backend/chapter-19

# Build and push with a specific tag
./containerization/build-and-push.sh ansgohar v1.0.0

# Or use 'latest' tag
./containerization/build-and-push.sh ansgohar latest
```

The script will automatically:
1. Check Docker installation and daemon status
2. Prompt for Docker Hub login
3. Build all 4 microservices from their Dockerfiles
4. Tag images with your Docker Hub username
5. Push images to Docker Hub
6. Verify successful push

### Manual Build and Push Steps

If you prefer to build and push manually:

#### Step 1: Build Docker Images Locally

From the chapter-19 directory:

```bash
docker-compose -f containerization/docker-compose.yml build
```

Or build individually:

```bash
docker build -t eureka-server:latest ./eureka-server
docker build -t gateway-server:latest ./gateway-server
docker build -t inventory-service:latest ./inventory-ms
docker build -t user-service:latest ./user-ms
```

#### Step 2: Authenticate with Docker Hub

```bash
docker login
# Enter your Docker Hub username and password (or access token)
```

#### Step 3: Tag Images for Docker Hub

```bash
# Replace 'ansgohar' with your Docker Hub username and 'v1.0.0' with your desired tag
docker tag eureka-server:latest ansgohar/eureka-server:v1.0.0
docker tag gateway-server:latest ansgohar/gateway-server:v1.0.0
docker tag inventory-service:latest ansgohar/inventory-service:v1.0.0
docker tag user-service:latest ansgohar/user-service:v1.0.0
```

#### Step 4: Push Images to Docker Hub

```bash
docker push ansgohar/eureka-server:v1.0.0
docker push ansgohar/gateway-server:v1.0.0
docker push ansgohar/inventory-service:v1.0.0
docker push ansgohar/user-service:v1.0.0
```

#### Step 5: Configure docker-compose.yml to Use Docker Hub Images

The `docker-compose.yml` file is already configured to use environment variables for image tags:

```yaml
eureka-server:
  image: ansgohar/eureka-server:${IMAGE_TAG:-latest}
```

Set the `IMAGE_TAG` environment variable before running:

```bash
# Set in .env file
echo "IMAGE_TAG=v1.0.0" >> containerization/.env

# Or export before running
export IMAGE_TAG=v1.0.0
docker-compose -f containerization/docker-compose.yml pull
```

## Running the Stack

### Option 1: Pull Pre-built Images from Docker Hub

If images are already pushed to Docker Hub:

```bash
cd containerization

# Set the image tag you want to use
export IMAGE_TAG=v1.0.0

# Or add to .env file
echo "IMAGE_TAG=v1.0.0" > .env

# Pull and start services
docker-compose pull
docker-compose up -d
```

### Option 2: Build Locally and Run

```bash
cd containerization
docker-compose up --build -d
```

### Check Service Status

```bash
# View running containers
docker-compose ps

# Check logs for a specific service
docker-compose logs -f eureka-server
docker-compose logs -f inventory-service
docker-compose logs -f user-service
docker-compose logs -f gateway-server
```

### Access Services

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Inventory Service (Direct)**: http://localhost:8081/inventory
- **User Service (Direct)**: http://localhost:8082/user
- **Zipkin Tracing**: http://localhost:9411/zipkin/
- **PostgreSQL**: localhost:5432 (user: bookstore, password: bookstore123)
- **MongoDB**: localhost:27017 (user: bookstore, password: bookstore123)

## Health Checks

All services include health checks configured to run every 10 seconds. The docker-compose.yml uses these health checks to ensure proper startup order.

To manually verify service health:

```bash
# Eureka Server
curl http://localhost:8761/eureka/status

# Inventory Service
curl http://localhost:8081/inventory/actuator/health

# User Service
curl http://localhost:8082/user/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health

# Zipkin
curl http://localhost:9411/zipkin/api/v2/services
```

## Database Configuration

### PostgreSQL (Inventory Service)

**Connection Details:**
- Host: postgres (or localhost:5432 from host machine)
- User: bookstore
- Password: bookstore123
- Database: inventory
- Port: 5432

**Data Persistence:**
- Data is stored in the `postgres_data` volume
- Volume is created automatically by docker-compose

### MongoDB (User Service)

**Connection Details:**
- Host: mongodb (or localhost:27017 from host machine)
- User: bookstore
- Password: bookstore123
- Database: userDB
- Authentication Database: admin
- Port: 27017

**Data Persistence:**
- Data is stored in the `mongo_data` volume
- Volume is created automatically by docker-compose

## Stopping and Cleaning Up

### Stop All Services

```bash
docker-compose down
```

### Remove Everything Including Volumes

```bash
docker-compose down -v
```

### Remove Specific Images

```bash
docker rmi <your-dockerhub-username>/eureka-server:latest
docker rmi <your-dockerhub-username>/gateway-server:latest
docker rmi <your-dockerhub-username>/inventory-service:latest
docker rmi <your-dockerhub-username>/user-service:latest
```

## Troubleshooting

### Services Not Starting

1. Check logs for the specific service:
   ```bash
   docker-compose logs <service-name>
   ```

2. Verify dependencies are healthy:
   ```bash
   docker-compose ps
   ```

3. Ensure ports are not in use:
   ```bash
   lsof -i :<port-number>
   ```

### Database Connection Issues

1. **Inventory Service cannot connect to PostgreSQL:**
   - Ensure PostgreSQL is healthy: `docker-compose ps postgres`
   - Verify JDBC URL: `jdbc:postgresql://postgres:5432/inventory`
   - Check credentials in environment variables

2. **User Service cannot connect to MongoDB:**
   - Ensure MongoDB is healthy: `docker-compose ps mongodb`
   - Verify connection string uses `mongodb` hostname
   - Check credentials match environment variables

### Services Cannot Register with Eureka

1. Verify Eureka Server is running and healthy
2. Check Eureka Server logs for errors
3. Ensure all services can reach the Eureka Server hostname (`eureka-server`)
4. Verify the Eureka URL in microservice environment variables

## Environment Variables

All services use the following environment variables (configurable in docker-compose.yml):

### Common to All Services

```
SPRING_APPLICATION_NAME=<service-name>
SERVER_PORT=<port>
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
EUREKA_INSTANCE_HOSTNAME=<service-name>
EUREKA_INSTANCE_PREFER_IP_ADDRESS=true
MANAGEMENT_TRACING_SAMPLING_PROBABILITY=1.0
ZIPKIN_BASEURL=http://zipkin:9411
```

### Inventory Service

```
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/inventory
SPRING_DATASOURCE_USERNAME=bookstore
SPRING_DATASOURCE_PASSWORD=bookstore123
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

### User Service

```
SPRING_DATA_MONGODB_HOST=mongodb
SPRING_DATA_MONGODB_PORT=27017
SPRING_DATA_MONGODB_DATABASE=userDB
SPRING_DATA_MONGODB_USERNAME=bookstore
SPRING_DATA_MONGODB_PASSWORD=bookstore123
SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin
```

## Dockerfile Details

Each microservice uses a multi-stage Docker build:

1. **Builder Stage**: Uses `maven:3.9-eclipse-temurin-21` to compile the application
2. **Runtime Stage**: Uses `eclipse-temurin:21-jre-alpine` for a lightweight runtime
3. **Health Checks**: Each container includes a health check that verifies the service is accessible

### Advantages of This Approach

- **Smaller Image Size**: Only runtime dependencies are included in the final image
- **Better Performance**: Alpine Linux is lightweight
- **Security**: Fewer vulnerabilities with minimal image
- **Reliability**: Health checks ensure services are ready before being marked as healthy

## Networking

All services communicate through a custom Docker network named `bookstore-network`. This provides:

- Internal DNS resolution (e.g., `eureka-server`, `postgres`, `mongodb`)
- Isolated network from other containers
- Better security and resource management

## Performance Optimization

### Build Time Optimization

The Dockerfiles leverage Maven's dependency caching:

1. First, `pom.xml` is copied and dependencies are downloaded
2. Then, source code is copied and compiled

This allows Docker to cache the dependency layer and skip re-downloading on source code changes.

### Runtime Optimization

- **Alpine Linux**: Reduces image size by ~80% compared to full JDK images
- **JRE Only**: Uses JRE instead of JDK, reducing image size
- **Health Checks**: Ensures only healthy services receive traffic

## Pushing to Docker Hub - Complete Workflow

```bash
#!/bin/bash
# Set your Docker Hub username
DOCKER_HUB_USERNAME="your-username"

# Login to Docker Hub
docker login

# Navigate to project directory
cd /Users/ahmadgohar/GitHub/Packt/Spring-Boot-and-Angular-2E_Backend/chapter-19

# Build all services
docker-compose build

# Tag and push each service
docker tag chapter-19-eureka-server:latest $DOCKER_HUB_USERNAME/eureka-server:latest
docker tag chapter-19-gateway-server:latest $DOCKER_HUB_USERNAME/gateway-server:latest
docker tag chapter-19-inventory-service:latest $DOCKER_HUB_USERNAME/inventory-service:latest
docker tag chapter-19-user-service:latest $DOCKER_HUB_USERNAME/user-service:latest

# Push to Docker Hub
docker push $DOCKER_HUB_USERNAME/eureka-server:latest
docker push $DOCKER_HUB_USERNAME/gateway-server:latest
docker push $DOCKER_HUB_USERNAME/inventory-service:latest
docker push $DOCKER_HUB_USERNAME/user-service:latest

echo "All images pushed to Docker Hub!"
```

## Production Deployment Considerations

1. **Use specific version tags** instead of `latest` for reproducibility
2. **Implement secrets management** for sensitive credentials (use Docker secrets or environment-specific configs)
3. **Use resource limits** to prevent containers from consuming excessive resources
4. **Implement logging** to a centralized log aggregation service
5. **Use a container orchestration platform** (Kubernetes) for production deployments
6. **Enable container restart policies** for automatic recovery
7. **Implement backup strategies** for databases
8. **Use private registries** for corporate Docker images

## Further Improvements

- Add Spring Boot Actuator metrics to Prometheus
- Implement distributed logging with ELK stack
- Add API rate limiting and throttling
- Implement API versioning
- Add database backups and recovery procedures
- Implement blue-green deployments
- Add automated health monitoring and alerting

## Support and Additional Resources

- Docker Documentation: https://docs.docker.com/
- Docker Compose Reference: https://docs.docker.com/compose/
- Spring Boot Docker Guide: https://spring.io/guides/topical/spring-boot-docker/
- Eureka Documentation: https://github.com/Netflix/eureka
- Spring Cloud Gateway: https://cloud.spring.io/spring-cloud-gateway/

## Maintenance

### Regular Tasks

- Monitor container resource usage
- Update base images for security patches
- Review and archive logs
- Backup database volumes regularly
- Test disaster recovery procedures

### Updating Services

To update a service after code changes:

```bash
# Rebuild the specific service
docker-compose build inventory-service

# Restart the service
docker-compose up -d inventory-service

# Check logs
docker-compose logs -f inventory-service
```

---

**Created**: January 2, 2026
**Version**: 1.0
**Author**: DevOps Engineer
