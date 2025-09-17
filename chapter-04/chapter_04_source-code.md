# Chapter 03 – Setting up Databases and Repositories

This guide provides step-by-step instructions to set up the **Inventory (PostgreSQL + JPA)** and **User Management (MongoDB + Spring Data MongoDB)** microservices. It also explains how to configure their databases and verify connectivity.

---

## 📦 Prerequisites
- Java 24+
- Maven 3.9+
- Docker (for PostgreSQL and MongoDB containers)
- VS Code with extensions:
  - **PostgreSQL (by Microsoft)**
  - **MongoDB for VS Code (by MongoDB Inc.)**
  - **GitHub Copilot** (optional, for code assistance)

---

## ⚙️ Step 1: Start Databases with Docker

Run PostgreSQL (for Inventory service):
```bash
docker run -d \
  --name bookstore-postgres \
  -e POSTGRES_USER=bookstore \
  -e POSTGRES_PASSWORD=bookstore123 \
  -e POSTGRES_DB=inventory \
  -p 5432:5432\
  postgres:17
```

Run MongoDB (for User Management service):
```bash
  docker run -d \
    --name bookstore-mongo \
    -e MONGO_INITDB_ROOT_USERNAME=bookstore \
    -e MONGO_INITDB_ROOT_PASSWORD=bookstore123 \
    -e MONGO_INITDB_DATABASE=userDB \
    -p 27017:27017 \
    mongo:8
```

---

## 📚 Step 2: Configure Inventory Microservice (PostgreSQL)

Update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory
    username: bookstore
    password: bookstore123
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true
    open-in-view: false

  sql:
    init:
      mode: always

logging:
  level:
    "[org.hibernate.SQL]": DEBUG
    "[org.hibernate.type.descriptor.sql]": TRACE
```

Entities:
- `Book`
- `Author`

Repositories:
- `BookRepository`
- `AuthorRepository`

---

## 👤 Step 3: Configure User Management Microservice (MongoDB)

Update `application.yml`:
```yaml
spring:
  application:
    name: user-ms
  data:
    mongodb:
      host: localhost
      port: 27017
      database: userDB
      username: bookstore
      password: bookstore123
      authentication-database: admin

server:
  port: 8082
```

Documents:
- `User`
- `Profile`
- `Address`
- `Preferences`

Repository:
- `UserRepository`

---

## 🔍 Step 4: Verify Databases in VS Code

1. Open the **PostgreSQL extension** → Connect to `inventory` DB.
   ```sql
   SELECT * FROM books;
   SELECT * FROM authors;
   ```

2. Open the **MongoDB extension** → Connect to `users` DB.
   ```json
   db.users.find().pretty()
   ```

---

## ✅ Step 5: Run Repository Tests

For JPA:
```java
@DataJpaTest
class BookRepositoryTest {
    // Test cases for BookRepository
}
```

For MongoDB:
```java
@DataMongoTest
class UserRepositoryTest {
    // Test cases for UserRepository
}
```

For Full Testing:
```java
@SpringBootTest
class UserRepositoryTest {
    // Test cases for UserRepository
}
```
---

## 🎯 Conclusion

At this point, both microservices are configured with their respective databases. You can:
- Run tests to validate queries.
- Use VS Code extensions to inspect tables and documents.
- Extend repositories with custom queries or projections.

---
