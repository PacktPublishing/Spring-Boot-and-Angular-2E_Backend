#!/bin/bash

# Docker Build, Tag, and Push Script for Bookstore Microservices
# Usage: ./build-and-push.sh [docker-username] [tag]
# Example: ./build-and-push.sh ansgohar latest

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get Docker Hub username from parameter or prompt
DOCKER_USERNAME=${1:-}
IMAGE_TAG=${2:-latest}

# Functions
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

# Check if Docker is installed and running
check_docker() {
    print_info "Checking Docker installation..."
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    
    if ! docker ps &> /dev/null; then
        print_error "Docker daemon is not running"
        exit 1
    fi
    
    print_success "Docker is installed and running"
}

# Check if Docker Compose is installed
check_docker_compose() {
    print_info "Checking Docker Compose installation..."
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed"
        exit 1
    fi
    
    print_success "Docker Compose is installed"
}

# Get Docker username if not provided
get_docker_username() {
    if [ -z "$DOCKER_USERNAME" ]; then
        print_warning "Docker username not provided"
        read -p "Enter your Docker Hub username: " DOCKER_USERNAME
        
        if [ -z "$DOCKER_USERNAME" ]; then
            print_error "Docker username is required"
            exit 1
        fi
    fi
    print_info "Using Docker username: $DOCKER_USERNAME"
}

# Login to Docker Hub
login_docker() {
    print_header "Docker Hub Login"
    
    echo "Logging in to Docker Hub..."
    if docker login; then
        print_success "Successfully logged in to Docker Hub"
    else
        print_error "Failed to login to Docker Hub"
        exit 1
    fi
}

# Build Docker images
build_images() {
    print_header "Building Docker Images"
    
    echo "Building all images (this may take 5-10 minutes)..."
    
    SERVICES=("eureka-server" "gateway-server" "inventory-service" "user-service")
    SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
    PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
    
    for service in "${SERVICES[@]}"; do
        if [ "$service" = "inventory-service" ]; then
            service_dir="inventory-ms"
        elif [ "$service" = "user-service" ]; then
            service_dir="user-ms"
        else
            service_dir="$service"
        fi
        
        print_info "Building $service from $service_dir/Dockerfile..."
        
        if docker build -t "$service:latest" -f "$PROJECT_ROOT/$service_dir/Dockerfile" "$PROJECT_ROOT/$service_dir"; then
            print_success "Built $service"
        else
            print_error "Failed to build $service"
            exit 1
        fi
    done
}

# Tag images
tag_images() {
    print_header "Tagging Images for Docker Hub"
    
    SERVICES=("eureka-server" "gateway-server" "inventory-service" "user-service")
    
    for service in "${SERVICES[@]}"; do
        local_image="${service}:latest"
        remote_image="${DOCKER_USERNAME}/${service}:${IMAGE_TAG}"
        
        print_info "Tagging: $local_image → $remote_image"
        
        if docker tag "$local_image" "$remote_image"; then
            print_success "Tagged $service"
        else
            print_error "Failed to tag $service"
            exit 1
        fi
    done
}

# Push images to Docker Hub
push_images() {
    print_header "Pushing Images to Docker Hub"
    
    SERVICES=("eureka-server" "gateway-server" "inventory-service" "user-service")
    
    for service in "${SERVICES[@]}"; do
        remote_image="${DOCKER_USERNAME}/${service}:${IMAGE_TAG}"
        
        print_info "Pushing $remote_image..."
        
        if docker push "$remote_image"; then
            print_success "Pushed $remote_image"
        else
            print_error "Failed to push $remote_image"
            exit 1
        fi
    done
}

# Verify images
verify_images() {
    print_header "Verifying Images on Docker Hub"
    
    SERVICES=("eureka-server" "gateway-server" "inventory-service" "user-service")
    
    for service in "${SERVICES[@]}"; do
        remote_image="${DOCKER_USERNAME}/${service}:${IMAGE_TAG}"
        
        print_info "Checking if $remote_image exists on Docker Hub..."
        
        if docker pull "$remote_image" &> /dev/null; then
            print_success "Verified $remote_image"
        else
            print_warning "Could not verify $remote_image (may still be processing)"
        fi
    done
}

# Show summary
show_summary() {
    print_header "Build, Tag, and Push Complete!"
    
    echo "Your images are now available on Docker Hub:"
    echo ""
    
    SERVICES=("eureka-server" "gateway-server" "inventory-service" "user-service")
    
    for service in "${SERVICES[@]}"; do
        remote_image="${DOCKER_USERNAME}/${service}:${IMAGE_TAG}"
        echo "  • $remote_image"
    done
    
    echo ""
    echo "To use these images, update your docker-compose.yml file:"
    echo ""
    echo "  image: ${DOCKER_USERNAME}/eureka-server:${IMAGE_TAG}"
    echo "  image: ${DOCKER_USERNAME}/gateway-server:${IMAGE_TAG}"
    echo "  image: ${DOCKER_USERNAME}/inventory-service:${IMAGE_TAG}"
    echo "  image: ${DOCKER_USERNAME}/user-service:${IMAGE_TAG}"
    echo ""
    echo "Then run:"
    echo "  docker-compose pull"
    echo "  docker-compose up -d"
    echo ""
}

# Main execution
main() {
    print_header "Bookstore Microservices - Build, Tag & Push Script"
    
    print_info "Docker Username: ${DOCKER_USERNAME:-Not set}"
    print_info "Image Tag: $IMAGE_TAG"
    echo ""
    
    check_docker
    check_docker_compose
    get_docker_username
    
    echo ""
    read -p "Do you want to continue? (y/n) " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Cancelled"
        exit 0
    fi
    
    login_docker
    build_images
    tag_images
    push_images
    verify_images
    show_summary
    
    print_success "All tasks completed successfully!"
}

# Run main function
main
