# Getting Started - Step by Step Guide

## Prerequisites

- Docker (v20.10+)
- Docker Compose (v2.0+)
- Docker Hub account (optional, for pushing images)

## Configuration Before Running

Before starting the services, review and update the following configuration:

### 1. Check Available Ports

Ensure these ports are not already in use:

| Service | Port | Status Check |
|---------|------|--------------|
| API Gateway | 8080 | `lsof -i :8080` |
| Inventory Service | 8081 | `lsof -i :8081` |
| User Service | 8082 | `lsof -i :8082` |
| Eureka Server | 8761 | `lsof -i :8761` |
| PostgreSQL | 5432 | `lsof -i :5432` |
| MongoDB | 27017 | `lsof -i :27017` |
| Zipkin | 9411 | `lsof -i :9411` |

**If a port is already in use:**
- Option 1: Stop the service using that port
- Option 2: Edit `containerization/docker-compose.yml` and change the port mapping (e.g., `"8090:8080"` to use port 8090 instead of 8080)

### 2. Environment Variables (Optional)

Copy the example environment file:
```bash
cp containerization/.env.example containerization/.env
```

Then edit `containerization/.env` to customize:
- Database credentials (if different from default)
- Port numbers (if you changed them above)
- Timezone settings
- Logging levels

Default credentials (can be changed in .env):
- PostgreSQL User: `bookstore`
- PostgreSQL Password: `bookstore123`
- MongoDB User: `bookstore`
- MongoDB Password: `bookstore123`

### 3. Docker Hub Configuration (Optional)

If you plan to use Docker Hub images with specific version tags:

1. Create a Docker Hub account at https://hub.docker.com
2. Add the IMAGE_TAG to your `.env` file:
   ```bash
   echo "IMAGE_TAG=v1.0.0" >> containerization/.env
   ```
3. Or export before running:
   ```bash
   export IMAGE_TAG=v1.0.0
   ```

For now, you can skip this - the local build will use the default "latest" tag.

### 4. Check Docker Daemon

Verify Docker is running:
```bash
docker ps
```

If this fails, start Docker Desktop (on Mac) or start the Docker service.

### 5. Verify Project Structure

Ensure you have all required files in `containerization/` folder:
```bash
ls -la containerization/
```

Should show:
- ✓ docker-compose.yml
- ✓ README.md
- ✓ GETTING_STARTED.md
- ✓ .env.example
- ✓ manage-services.sh

And Dockerfiles in each service directory:
```bash
ls Dockerfile eureka-server/ gateway-server/ inventory-ms/ user-ms/
```

Should show 4 Dockerfile files.

## Quick Start (5 Minutes)

### Step 1: Navigate to Project Directory
```bash
cd /Users/ahmadgohar/GitHub/Packt/Spring-Boot-and-Angular-2E_Backend/chapter-19
```

### Step 2: Build and Start All Services
```bash
docker-compose -f containerization/docker-compose.yml up --build -d
```

This command will:
- Build Docker images for all 4 microservices
- Start 7 containers (4 microservices + PostgreSQL + MongoDB + Zipkin)
- Initialize databases
- Register services with Eureka

### Step 3: Wait for Services to Be Ready
Services take 30-60 seconds to fully initialize. Monitor progress:
```bash
docker-compose -f containerization/docker-compose.yml ps
```

All services should show "healthy" status.

### Step 4: Verify Services Are Running
```bash
# Check if Eureka is responding
curl http://localhost:8761/eureka/status

# Check if Gateway is responding
curl http://localhost:8080/actuator/health

# Check if Inventory Service is responding
curl http://localhost:8081/inventory/actuator/health

# Check if User Service is responding
curl http://localhost:8082/user/actuator/health
```

### Step 5: Access Dashboards

Open these URLs in your browser:

| Service | URL |
|---------|-----|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| Inventory Service | http://localhost:8081/inventory |
| User Service | http://localhost:8082/user |
| Zipkin Tracing | http://localhost:9411/zipkin/ |

## Common Commands

### View Service Logs
```bash
# All services
docker-compose -f containerization/docker-compose.yml logs -f

# Specific service
docker-compose -f containerization/docker-compose.yml logs -f eureka-server
docker-compose -f containerization/docker-compose.yml logs -f inventory-service
docker-compose -f containerization/docker-compose.yml logs -f user-service
docker-compose -f containerization/docker-compose.yml logs -f gateway-server
```

### Stop All Services
```bash
docker-compose -f containerization/docker-compose.yml down
```

### Stop Services and Remove Data
```bash
docker-compose -f containerization/docker-compose.yml down -v
```

### Restart a Service
```bash
docker-compose -f containerization/docker-compose.yml restart inventory-service
```

## Using the Management Script

For an interactive menu to manage services:

```bash
# Make script executable (first time only)
chmod +x containerization/manage-services.sh

# Run the script
./containerization/manage-services.sh
```

Menu options include:
- Build and start services
- View service status
- View logs
- Restart services
- Push to Docker Hub
- Health checks

## Database Access

### PostgreSQL (Inventory Service)
```bash
# Connect to PostgreSQL
docker-compose -f containerization/docker-compose.yml exec postgres psql -U bookstore -d inventory

# Connection details:
# Host: localhost
# Port: 5432
# User: bookstore
# Password: bookstore123
# Database: inventory
```

### MongoDB (User Service)
```bash
# Connect to MongoDB
docker-compose -f containerization/docker-compose.yml exec mongodb mongosh -u bookstore -p bookstore123

# Connection details:
# Host: localhost
# Port: 27017
# User: bookstore
# Password: bookstore123
# Database: userDB
```

## Pushing to Docker Hub

### Option 1: Using the Automated Script (Recommended)

The easiest and safest way to build and push images:

```bash
# Navigate to project root
cd /Users/ahmadgohar/GitHub/Packt/Spring-Boot-and-Angular-2E_Backend/chapter-19

# Make the script executable (first time only)
chmod +x containerization/build-and-push.sh

# Run with your Docker Hub username and desired tag
./containerization/build-and-push.sh ansgohar v1.0.0

# Or use 'latest' tag
./containerization/build-and-push.sh ansgohar latest
```

The script will:
- ✓ Check Docker installation and verify daemon is running
- ✓ Prompt for Docker Hub login credentials
- ✓ Build all 4 microservices from their Dockerfiles
- ✓ Tag images with your Docker Hub username and version tag
- ✓ Push all images to Docker Hub
- ✓ Verify images were successfully pushed
- ✓ Display summary with next steps

**Example Output:**
```
Your images are now available on Docker Hub:
  • ansgohar/eureka-server:v1.0.0
  • ansgohar/gateway-server:v1.0.0
  • ansgohar/inventory-service:v1.0.0
  • ansgohar/user-service:v1.0.0
```

### Option 2: Manual Steps

If you prefer to build and push manually:

#### Step 1: Build Images
```bash
docker-compose -f containerization/docker-compose.yml build
```

#### Step 2: Login to Docker Hub
```bash
docker login
# Enter your Docker Hub username and password
```

#### Step 3: Tag Images
```bash
# Replace 'ansgohar' with your Docker Hub username and 'v1.0.0' with your tag
docker tag eureka-server:latest ansgohar/eureka-server:v1.0.0
docker tag gateway-server:latest ansgohar/gateway-server:v1.0.0
docker tag inventory-service:latest ansgohar/inventory-service:v1.0.0
docker tag user-service:latest ansgohar/user-service:v1.0.0
```

#### Step 4: Push Images
```bash
docker push ansgohar/eureka-server:v1.0.0
docker push ansgohar/gateway-server:v1.0.0
docker push ansgohar/inventory-service:v1.0.0
docker push ansgohar/user-service:v1.0.0
```

#### Step 5: Update .env File
```bash
# Add or update the IMAGE_TAG variable
echo "IMAGE_TAG=v1.0.0" >> containerization/.env
```

### Using Docker Hub Images

Once images are pushed, you can pull and run them:

```bash
cd containerization

# Pull latest images (uses IMAGE_TAG from .env or defaults to 'latest')
export IMAGE_TAG=v1.0.0
docker-compose pull

# Start services with pulled images
docker-compose up -d
```

## Troubleshooting

### Services Won't Start
```bash
# Check logs
docker-compose -f containerization/docker-compose.yml logs

# Check if ports are already in use
lsof -i :8080
lsof -i :8081
lsof -i :8082
lsof -i :8761
lsof -i :5432
lsof -i :27017
```

### Database Connection Failed
```bash
# Check PostgreSQL is running
docker-compose -f containerization/docker-compose.yml exec postgres pg_isready -U bookstore

# Check MongoDB is running
docker-compose -f containerization/docker-compose.yml exec mongodb mongosh --eval "db.adminCommand('ping')"
```

### Service Registered but Not Responding
```bash
# View Eureka dashboard to see registered services
# http://localhost:8761

# Check service logs
docker-compose -f containerization/docker-compose.yml logs <service-name>

# Wait a bit longer - services take time to initialize
sleep 30
docker-compose -f containerization/docker-compose.yml ps
```

### Port Already in Use
If you get "port already allocated" error:

```bash
# Find what's using the port
lsof -i :8080

# Either kill that process or change the port in docker-compose.yml
# In docker-compose.yml, change: "8080:8080" to "8090:8080"
```

## Architecture

```
Clients
  ↓
API Gateway (8080)
  ↓
Service Registry (Eureka - 8761)
  ├─→ Inventory Service (8081) → PostgreSQL (5432)
  ├─→ User Service (8082) → MongoDB (27017)
  └─→ Zipkin (9411) [distributed tracing]
```

## Service Startup Order

The docker-compose.yml ensures services start in this order:
1. PostgreSQL
2. MongoDB
3. Zipkin
4. Eureka Server
5. Inventory Service
6. User Service
7. API Gateway

## Environment Configuration

Copy and customize environment variables:
```bash
cp containerization/.env.example containerization/.env
# Edit .env to change database passwords, ports, etc.
```

## Next Steps

1. ✅ Start the services: `docker-compose up --build -d`
2. ✅ Verify with health checks
3. ✅ Access Eureka dashboard
4. ✅ View traces in Zipkin
5. ✅ Read [README.md](README.md) for detailed information
6. ✅ Push to Docker Hub (optional)

## Support

For detailed information, see [README.md](README.md)

Common issues and solutions are documented in the README.md file.
