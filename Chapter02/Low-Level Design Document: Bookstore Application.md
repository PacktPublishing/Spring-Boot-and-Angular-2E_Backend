Low-Level Design Document: Bookstore Application
1. Book Inventory Microservice
Technology Stack

Java 21, Spring Boot 3.x, Spring Data JPA, PostgreSQL.

Database Schema

Books: id (PK), title, author_id (FK), genre_id (FK), price, isbn, publication_date.
Authors: id (PK), name, bio.
Genres: id (PK), name.
Inventory: book_id (FK), stock_level.

REST APIs (HATEOAS)

GET /books: List books (filter by genre/price, sort by popularity/date).
GET /books/{id}: Get book details.
POST /books: Add book (admin, request: {title, authorId, genreId, price, isbn, date}).
PUT /books/{id}: Update book (admin).
DELETE /books/{id}: Delete book (admin).
GET /inventory/{bookId}: Get stock level.
PUT /inventory/{bookId}: Update stock (admin, request: {stockLevel}).

Features

Filtering/sorting via Spring Data JPA Specifications.
Pagination with Pageable.
Transactional stock updates.

Security

Admin-only endpoints secured with Spring Security (JWT).

2. User Management Microservice
Technology Stack

Java 21, Spring Boot 3.x, Spring Data MongoDB.

Database Collections

Users: {id, name, email, password_hash, roles, address, payment_methods}.
Orders: {id, user_id, items: [{book_id, quantity}], status, total, timestamp}.

REST APIs (HATEOAS)

POST /users: Register user (request: {name, email, password}).
GET /users/{id}: Get profile (self or admin).
PUT /users/{id}: Update profile (request: {name, address, payment_methods}).
POST /orders: Create order (request: {userId, items}).
GET /orders/{id}: Get order details.
GET /orders/user/{userId}: List user’s orders.

Features

Email verification on registration.
Order status updates via WebSocket.
Stock validation via REST call to inventory microservice.

Security

JWT authentication; roles: ROLE_USER, ROLE_ADMIN.

3. API Gateway
Technology Stack

Spring Cloud Gateway.

Configuration

Routes:
/books/** → Book Inventory Microservice.
/users/** → User Management Microservice.


Filters:
JWT validation filter.
Rate limiting (e.g., 100 requests/minute per user).
Circuit breaker (Hystrix or Resilience4j).



Features

Centralized request routing.
Load balancing across microservice instances.

4. Frontend (Angular)
Technology Stack

Angular 20, TypeScript.

Components

Book Catalog: Filter/sort books, lazy-loaded.
Book Details: Display book info, add-to-cart button.
Shopping Cart: Reactive state with Signals, real-time total.
Checkout: Reactive form, payment integration.
User Profile: Edit address/payment methods.
Order Tracking: WebSocket for real-time updates.

Features

State Management: Angular Signals for cart/user state.
SEO: Server-Side Rendering (SSR).
APIs: Consumes REST endpoints via HttpClient.

5. Security Implementation
Keycloak

Setup: Realm bookstore, clients for frontend and backend.
Roles: customer, admin.

Spring Security

Config: JWT filter, role-based access in microservices.
Endpoints: Secure admin routes (e.g., /books POST).

Angular

Guards: Protect routes (e.g., /profile).
Interceptors: Attach JWT to API requests.

6. UML Diagrams
Class Diagrams
Book Inventory Microservice

Classes:
Book: id, title, price, isbn, publication_date, Author author, Genre genre.
Author: id, name, bio.
Genre: id, name.
Inventory: book_id, stock_level.


Relationships:
Book → Author (many-to-one).
Book → Genre (many-to-one).
Inventory → Book (one-to-one).


Note: Create this diagram using a UML tool, showing attributes and associations.

User Management Microservice

Classes:
User: id, name, email, password_hash, roles, address, payment_methods.
Order: id, user_id, items: List<Item>, status, total, timestamp.
Item: book_id, quantity.


Relationships:
Order → User (many-to-one).
Order contains embedded Item list.


Note: Create this diagram using a UML tool, reflecting the MongoDB document structure.

Sequence Diagrams
Checkout Process

Actors: User, Frontend, API Gateway, User Management Microservice, Book Inventory Microservice.
Steps:
User clicks "Checkout" on Frontend.
Frontend sends POST /orders to API Gateway.
API Gateway routes to User Management Microservice.
User Management Microservice validates user and creates order.
User Management Microservice calls GET /inventory/{bookId} on Book Inventory Microservice.
Book Inventory Microservice checks stock, updates if available.
User Management Microservice confirms order, returns success to Frontend.


Note: Create this diagram using a UML tool, showing the flow of API calls.

7. ER Diagrams
PostgreSQL (Book Inventory)

Tables:
Books: id (PK), title, author_id (FK), genre_id (FK), price, isbn, publication_date.
Authors: id (PK), name, bio.
Genres: id (PK), name.
Inventory: book_id (FK, PK), stock_level.


Relationships:
Books.author_id → Authors.id.
Books.genre_id → Genres.id.
Inventory.book_id → Books.id.


Note: Create an ER diagram with primary/foreign keys and relationships.

MongoDB (User Management)

Collections:
Users: { _id, name, email, password_hash, roles, address, payment_methods }.
Orders: { _id, user_id, items: [{ book_id, quantity }], status, total, timestamp }.


Relationships:
Orders.user_id references Users._id.
items embedded as an array within Orders.


Note: Describe the document structure, no traditional ER diagram needed due to MongoDB’s schema-less nature.

8. Docker Compose Manifest
version: '3.8'
services:
  frontend:
    build: ./frontend
    ports:
      - "4200:4200"
    depends_on:
      - api-gateway

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - book-inventory
      - user-management

  book-inventory:
    build: ./book-inventory
    ports:
      - "8081:8081"
    depends_on:
      - postgres

  user-management:
    build: ./user-management
    ports:
      - "8082:8082"
    depends_on:
      - mongo

  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: bookstore
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"

  mongo:
    image: mongo:5.0
    ports:
      - "27017:27017"

  keycloak:
    image: jboss/keycloak:16.1.1
    environment:
      KEYCLOAK_USER: admin
      KEYCLOAK_PASSWORD: admin
    ports:
      - "8083:8080"


Purpose: Sets up the full stack for local development.

9. GitHub Actions CI/CD Pipeline
Backend Workflow
name: Backend CI/CD

on:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v2

      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
          distribution: 'adopt'

      - name: Build with Maven
        run: mvn clean package

      - name: Run tests
        run: mvn test

      - name: Build Docker image
        run: docker build -t bookstore-backend .

      - name: Push to Docker Hub
        run: |
          echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
          docker tag bookstore-backend ${{ secrets.DOCKER_USERNAME }}/bookstore-backend:latest
          docker push ${{ secrets.DOCKER_USERNAME }}/bookstore-backend:latest

Frontend Workflow
name: Frontend CI/CD

on:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v2

      - name: Set up Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '20'

      - name: Install dependencies
        run: npm install

      - name: Run tests
        run: npm test

      - name: Build Angular app
        run: npm run build --prod

      - name: Build Docker image
        run: docker build -t bookstore-frontend .

      - name: Push to Docker Hub
        run: |
          echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
          docker tag bookstore-frontend ${{ secrets.DOCKER_USERNAME }}/bookstore-frontend:latest
          docker push ${{ secrets.DOCKER_USERNAME }}/bookstore-frontend:latest

10. Additional Considerations

API Documentation: OpenAPI specs via Springdoc.
Logging: Centralized with ELK stack.
Testing: Unit tests (JUnit, Jasmine), integration tests (TestRestTemplate, Karma).

