#!/bin/bash

# Bookstore Microservices - Docker Compose Quick Start Script
# This script helps you manage the entire containerized microservices stack

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Utility functions
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    print_success "Docker found: $(docker --version)"
}

# Check if Docker Compose is installed
check_compose() {
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    print_success "Docker Compose found: $(docker-compose --version)"
}

# Check if Docker daemon is running
check_docker_daemon() {
    if ! docker ps &> /dev/null; then
        print_error "Docker daemon is not running. Please start Docker."
        exit 1
    fi
    print_success "Docker daemon is running"
}

# Show menu
show_menu() {
    print_header "Bookstore Microservices Management"
    
    echo "Select an option:"
    echo ""
    echo "1) Build and start all services"
    echo "2) Build services only (don't start)"
    echo "3) Start all services"
    echo "4) Stop all services"
    echo "5) View service status"
    echo "6) View logs for a service"
    echo "7) View logs for all services"
    echo "8) Restart a service"
    echo "9) Push images to Docker Hub"
    echo "10) Clean up (remove stopped containers & unused images)"
    echo "11) Full cleanup (remove all containers, volumes & images)"
    echo "12) Show service URLs"
    echo "13) Run health checks"
    echo "0) Exit"
    echo ""
}

# Build services
build_services() {
    print_header "Building Docker Images"
    
    if docker-compose build; then
        print_success "All images built successfully"
    else
        print_error "Failed to build images"
        exit 1
    fi
}

# Start services
start_services() {
    print_header "Starting Services"
    
    if docker-compose up -d; then
        print_success "All services started"
        echo ""
        print_info "Waiting for services to be ready (this may take 1-2 minutes)..."
        sleep 30
        echo ""
        show_service_status
    else
        print_error "Failed to start services"
        exit 1
    fi
}

# Stop services
stop_services() {
    print_header "Stopping Services"
    
    if docker-compose down; then
        print_success "All services stopped"
    else
        print_error "Failed to stop services"
        exit 1
    fi
}

# Show service status
show_service_status() {
    print_header "Service Status"
    docker-compose ps
}

# View logs
view_logs() {
    print_header "Select a Service to View Logs"
    
    services=("eureka-server" "inventory-service" "user-service" "gateway-server" "postgres" "mongodb" "zipkin")
    
    echo "Available services:"
    for i in "${!services[@]}"; do
        echo "$((i+1))) ${services[$i]}"
    done
    echo ""
    read -p "Enter service number: " service_choice
    
    service_number=$((service_choice - 1))
    if [ $service_number -ge 0 ] && [ $service_number -lt ${#services[@]} ]; then
        selected_service="${services[$service_number]}"
        print_header "Logs for $selected_service"
        docker-compose logs -f "$selected_service"
    else
        print_error "Invalid selection"
    fi
}

# View all logs
view_all_logs() {
    print_header "Logs for All Services"
    docker-compose logs -f
}

# Restart service
restart_service() {
    print_header "Select a Service to Restart"
    
    services=("eureka-server" "inventory-service" "user-service" "gateway-server")
    
    echo "Microservices:"
    for i in "${!services[@]}"; do
        echo "$((i+1))) ${services[$i]}"
    done
    echo ""
    read -p "Enter service number: " service_choice
    
    service_number=$((service_choice - 1))
    if [ $service_number -ge 0 ] && [ $service_number -lt ${#services[@]} ]; then
        selected_service="${services[$service_number]}"
        print_header "Restarting $selected_service"
        docker-compose restart "$selected_service"
        print_success "$selected_service restarted"
    else
        print_error "Invalid selection"
    fi
}

# Push to Docker Hub
push_to_docker_hub() {
    print_header "Push Images to Docker Hub"
    
    read -p "Enter your Docker Hub username: " docker_username
    read -p "Enter image tag (default: latest): " image_tag
    image_tag=${image_tag:-latest}
    
    print_info "Logging in to Docker Hub..."
    docker login
    
    services=("eureka-server" "gateway-server" "inventory-service" "user-service")
    
    echo ""
    print_header "Pushing Images"
    
    for service in "${services[@]}"; do
        local_image="chapter-19-${service}:${image_tag}"
        remote_image="${docker_username}/${service}:${image_tag}"
        
        print_info "Processing: $service"
        
        # Tag the image
        docker tag "$local_image" "$remote_image"
        
        # Push to Docker Hub
        if docker push "$remote_image"; then
            print_success "Pushed $remote_image"
        else
            print_error "Failed to push $remote_image"
        fi
    done
    
    echo ""
    print_info "Update your docker-compose.yml to use these images:"
    for service in "${services[@]}"; do
        echo "  image: ${docker_username}/${service}:${image_tag}"
    done
}

# Cleanup
cleanup() {
    print_header "Cleanup"
    
    echo "This will remove stopped containers and unused images."
    read -p "Continue? (y/n) " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker system prune -f
        print_success "Cleanup complete"
    fi
}

# Full cleanup
full_cleanup() {
    print_header "Full Cleanup"
    
    echo "This will remove ALL containers, volumes, and images related to this project."
    print_warning "This action cannot be undone!"
    read -p "Continue? (y/n) " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v
        docker system prune -af
        print_success "Full cleanup complete"
    fi
}

# Show service URLs
show_urls() {
    print_header "Service URLs"
    
    echo "Eureka Server:        http://localhost:8761"
    echo "API Gateway:          http://localhost:8080"
    echo "Inventory Service:    http://localhost:8081/inventory"
    echo "User Service:         http://localhost:8082/user"
    echo "Zipkin Tracing:       http://localhost:9411/zipkin/"
    echo ""
    echo "Health Check URLs:"
    echo "Eureka Status:        http://localhost:8761/eureka/status"
    echo "Inventory Health:     http://localhost:8081/inventory/actuator/health"
    echo "User Health:          http://localhost:8082/user/actuator/health"
    echo "Gateway Health:       http://localhost:8080/actuator/health"
    echo ""
    echo "Database Connection Details:"
    echo "PostgreSQL:           localhost:5432 (user: bookstore, password: bookstore123)"
    echo "MongoDB:              localhost:27017 (user: bookstore, password: bookstore123)"
}

# Health checks
run_health_checks() {
    print_header "Running Health Checks"
    
    services=(
        "http://localhost:8761/eureka/status:Eureka Server"
        "http://localhost:8081/inventory/actuator/health:Inventory Service"
        "http://localhost:8082/user/actuator/health:User Service"
        "http://localhost:8080/actuator/health:API Gateway"
        "http://localhost:9411/zipkin/api/v2/services:Zipkin"
    )
    
    for service in "${services[@]}"; do
        IFS=':' read -r url name <<< "$service"
        if curl -s -f "$url" > /dev/null 2>&1; then
            print_success "$name is healthy"
        else
            print_error "$name is not responding"
        fi
    done
}

# Main script
main() {
    # Check prerequisites
    check_docker
    check_compose
    check_docker_daemon
    
    # Main loop
    while true; do
        show_menu
        read -p "Enter your choice [0-13]: " choice
        
        case $choice in
            1) build_services; start_services ;;
            2) build_services ;;
            3) start_services ;;
            4) stop_services ;;
            5) show_service_status ;;
            6) view_logs ;;
            7) view_all_logs ;;
            8) restart_service ;;
            9) push_to_docker_hub ;;
            10) cleanup ;;
            11) full_cleanup ;;
            12) show_urls ;;
            13) run_health_checks ;;
            0) print_info "Exiting..."; exit 0 ;;
            *) print_error "Invalid option. Please try again." ;;
        esac
        
        read -p "Press Enter to continue..."
    done
}

# Run main script
main
