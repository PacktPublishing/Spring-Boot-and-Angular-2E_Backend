# Bookstore Application - High-Level Design

## System Architecture
```plantuml
@startuml
!theme plain
skinparam componentStyle uml2

[Client Browser] as browser
[Angular 20 SPA] as angular
[Spring Cloud Gateway] as gateway
[User Management Microservice] as user_service
[Book Inventory Microservice] as book_service
[MongoDB] as mongo
[PostgreSQL] as postgres
[Keycloak] as keycloak

browser --> angular
angular --> gateway
gateway --> user_service
gateway --> book_service
user_service --> mongo
book_service --> postgres
keycloak --> gateway

@enduml


Technology Stack
Layer	Technology
Frontend	Angular 20, RxJS, SSR
API Gateway	Spring Cloud Gateway
Backend	Java 21, Spring Boot 3.5
SQL Database	PostgreSQL
NoSQL Database	MongoDB
Auth	Keycloak, Spring Security
Observability	Prometheus, Grafana, ELK
Deployment	Docker, Kubernetes
