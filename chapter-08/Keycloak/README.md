# Keycloak Docker Compose Setup

This document explains the Keycloak Docker Compose configuration for the Bookstore application and how to run it.

## Docker Compose File Explanation

### Version
```yaml
version: '3.8'
```
- Specifies the Docker Compose file format version (3.8 is compatible with Docker Engine 20.10+)

### Services
```yaml
services:
  keycloak:
```
- Defines a single service named `keycloak`

#### Image Configuration
```yaml
image: quay.io/keycloak/keycloak:26.0.0
```
- Uses the official Keycloak image from Quay.io repository
- Version: 26.0.0 (Latest stable version)

#### Container Name
```yaml
container_name: bookstore-keycloak
```
- Names the running container as `bookstore-keycloak`
- Makes it easier to identify and manage the container

#### Startup Command
```yaml
command: start-dev
```
- Starts Keycloak in development mode
- Enables faster startup and automatic database initialization
- Not recommended for production environments

#### Environment Variables
```yaml
environment:
  KEYCLOAK_ADMIN: admin
  KEYCLOAK_ADMIN_PASSWORD: admin
  KC_HTTP_PORT: 8090
```
- **KEYCLOAK_ADMIN**: Default admin username (set to `admin`)
- **KEYCLOAK_ADMIN_PASSWORD**: Default admin password (set to `admin`)
- **KC_HTTP_PORT**: HTTP port for Keycloak (changed from 8080 to 8090)

#### Port Mapping
```yaml
ports:
  - "8090:8090"
```
- Maps port 8090 on your host machine to port 8090 inside the container
- **Format**: `"host-port:container-port"`
- Access Keycloak at: `http://localhost:8090`

#### Network Configuration
```yaml
networks:
  - bookstore-network
```
- Connects the Keycloak container to the `bookstore-network`
- Enables communication with other services (eureka-server, gateway-server, user-ms, inventory-ms)

### Networks Definition
```yaml
networks:
  bookstore-network:
    driver: bridge
```
- Defines the `bookstore-network` bridge network
- **Bridge driver**: Allows containers to communicate with each other and the host
- Isolated from other Docker networks on the system

## How to Run

### Prerequisites
- Docker installed on your machine
- Docker Compose installed (usually comes with Docker Desktop)

### Starting Keycloak

1. Navigate to the Keycloak directory:
```bash
cd /Users/ahmadgohar/GitHub/Packt/Spring-Boot-and-Angular-2E_Backend/chapter-08/Keycloak
```

2. Start the Keycloak container:
```bash
docker-compose up -d
```
- `-d` flag runs the container in detached mode (background)

3. Wait for Keycloak to initialize (usually 30-60 seconds)

4. Access Keycloak Admin Console:
```
URL: http://localhost:8090/admin
Username: admin
Password: admin
```

### Viewing Logs
```bash
docker-compose logs -f keycloak
```
- `-f` flag follows the logs in real-time
- Press `Ctrl+C` to exit

### Stopping Keycloak
```bash
docker-compose down
```
- Stops and removes the container
- The network persists for other services

### Removing Everything
```bash
docker-compose down -v
```
- Removes the container, network, and any volumes

## Common Tasks

### View Running Container Status
```bash
docker ps
```

### Check Container Health
```bash
docker-compose ps
```

### Restart Keycloak
```bash
docker-compose restart keycloak
```

### Change Admin Credentials
Update the `KEYCLOAK_ADMIN` and `KEYCLOAK_ADMIN_PASSWORD` environment variables in the docker-compose.yml file before running the container.

## Network Communication

The `bookstore-network` enables:
- **Keycloak** to authenticate users for all microservices
- **Other services** (user-ms, inventory-ms) to validate tokens with Keycloak
- **Gateway Server** to act as an API gateway with Keycloak integration

All services using this network can reference Keycloak as `http://keycloak:8090` instead of `http://localhost:8090`.

## Development vs Production

⚠️ **Important**: The current configuration uses `start-dev` mode with simple credentials. For production:
1. Change credentials to strong passwords
2. Use `start` instead of `start-dev`
3. Configure a persistent database (PostgreSQL, MySQL)
4. Enable HTTPS
5. Set appropriate JVM memory limits

## Alternative: Using Docker Run Command

If you prefer a simpler approach without Docker Compose, you can use the `docker run` command to start Keycloak directly. This is useful when you're running Keycloak in isolation or testing it independently from other services. The `docker run` command allows you to specify all configuration options directly in the terminal without needing a separate compose file.

The Docker run approach is ideal for development environments where you want quick startup and teardown, or when you're running Keycloak on a machine that doesn't have all your microservices. You can easily modify environment variables, ports, and other settings by changing the command parameters. However, if you need service-to-service communication via Docker networks (when running multiple microservices), Docker Compose is the recommended approach.

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

### Docker Run Command Details

The `docker run` command creates and starts a new container with the `docker run` base command. The `-d` flag runs the container in detached mode (background), allowing you to continue using the terminal without waiting for the container to finish. The `--name bookstore-keycloak` option assigns a friendly name to the container for easy reference and management. The environment variables are set using the `-e` flag: `KEYCLOAK_ADMIN=admin` sets the admin username, `KEYCLOAK_ADMIN_PASSWORD=admin` sets the admin password, and `KC_HTTP_PORT=8090` configures Keycloak to listen on port 8090 inside the container. The port mapping is specified with `-p 8090:8090`, which maps port 8090 from the host machine to port 8090 inside the container, allowing you to access Keycloak from your browser. The `quay.io/keycloak/keycloak:26.0.0` is the image name and version to pull and run from the Quay.io registry. Finally, `start-dev` is the startup command passed to the container, which starts Keycloak in development mode with fast initialization and automatic database setup.

After running this command, access the Keycloak Admin Console at `http://localhost:8090/admin` with username and password both set to `admin`. To view the container logs in real-time, use `docker logs bookstore-keycloak -f`. To stop the container, run `docker stop bookstore-keycloak`, and to remove it completely (freeing up the name and resources), use `docker rm bookstore-keycloak`.

## Troubleshooting

### Port Already in Use
If port 8090 is already in use:
1. Change the host port in the `ports` section (e.g., `"8091:8090"`)
2. Or stop the service using that port

### Container Won't Start
```bash
docker-compose logs keycloak
```
Check logs for error messages and ensure Docker daemon is running.

### Can't Access Admin Console
1. Verify the container is running: `docker-compose ps`
2. Check if port 8090 is accessible: `curl http://localhost:8090/health`
3. Wait a bit longer for full initialization
