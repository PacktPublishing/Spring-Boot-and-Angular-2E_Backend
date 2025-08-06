# Chapter 03 – Setting up Databases and Repositories

This guide provides step-by-step instructions to set up the **Inventory (PostgreSQL + JPA)** and **User Management (MongoDB + Spring Data MongoDB)** microservices. It also explains how to configure their databases, verify connectivity, and describes the current class structures.

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
  -p 5432:5432 \
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

### **Class Structure**

#### Entities:
- **Book**: Represents a book in the inventory. Fields include `id`, `title`, `isbn`, `price`, `quantity`, `genre`, `published`, `description`, `pageCount`, `coverImageUrl`, and a many-to-one relationship to `Author`.
- **Author**: Represents an author. Fields include `id`, `name`, `nationality`, and a one-to-many relationship to `Book`.
- **Auditable**: A `@MappedSuperclass` providing `createdAt` and `updatedAt` fields for audit purposes, inherited by both `Book` and `Author`.

#### Repositories:
- **BookRepository**: Extends `JpaRepository<Book, Long>`, supports custom queries and specifications.
- **AuthorRepository**: Extends `JpaRepository<Author, Long>`, includes methods like `findByName`.

---

## 👤 Step 3: Configure User Management Microservice (MongoDB)

Update `application.yml`:
```yaml
spring:
  application:
    name: user-ms
  data:
    mongodb:
      uri: mongodb://bookstore:bookstore123@localhost:27017/userDB
      # authentication-database: admin # Uncomment if needed

server:
  port: 8082
```

### **Class Structure**

#### Documents:
- **User**: Main user document. Fields include `id`, `email`, `username`, `dateOfBirth`, `status`, `createdAt`, `lastLogin`, and embedded documents for `Profile` and `Preferences`.
- **Profile**: Embedded in `User`. Fields include `fullName`, `birthDate`, `phone`, and an embedded `Address`.
- **Address**: Embedded in `Profile`. Fields include `street`, `city`, `state`, `postalCode`, `country`.
- **Preferences**: Embedded in `User`. Fields include `language`, `newsletter`, `currency`, and `favoriteGenres`.

#### Repository:
- **UserRepository**: Extends `MongoRepository<User, String>`, supports queries like `findByEmail` and `findByUsernameContainingIgnoreCase`.

---

## 🔍 Step 4: Verify Databases in VS Code

1. Open the **PostgreSQL extension** → Connect to `inventory` DB.
   ```sql
   SELECT * FROM books;
   SELECT * FROM authors;
   ```

2. Open the **MongoDB extension** → Connect to `userDB` database.
   ```javascript
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

For Full Integration Testing:
```java
@SpringBootTest
class UserRepositoryTest {
    // Test cases for UserRepository
}
```

---

## 🗂️ Class Diagram Overview

### Inventory Microservice (JPA)
```plantuml
@startuml
class Auditable {
  +LocalDateTime createdAt
  +LocalDateTime updatedAt
  <<MappedSuperclass>>
}

class Author {
  +Long id
  +String name
  +String nationality
  +List<Book> books
  --
  <<Entity>>
  <<Table: authors>>
}

class Book {
  +Long id
  +String title
  +String isbn
  +BigDecimal price
  +int quantity
  +Author author
  +String genre
  +String published
  +String description
  +Integer pageCount
  +String coverImageUrl
  --
  <<Entity>>
  <<Table: books>>
}

Author "1" o-- "0..*" Book : has
Book "*" -- "1" Author : references

Book --|> Auditable
Author --|> Auditable
@enduml
```

### User Management Microservice (MongoDB)
```plantuml
@startuml
class User {
  +String id
  +String email
  +String username
  +LocalDateTime dateOfBirth
  +String status
  +Profile profile
  +Preferences preferences
  +LocalDateTime createdAt
  +LocalDateTime lastLogin
}

class Profile {
  +String fullName
  +String birthDate
  +String phone
  +Address address
}

class Preferences {
  +String language
  +Boolean newsletter
  +String currency
  +String[] favoriteGenres
}

class Address {
  +String street
  +String city
  +String state
  +String postalCode
  +String country
}

User "1" -- "1" Profile : has
User "1" -- "1" Preferences : has
Profile "1" -- "1" Address : has
@enduml
```

---

## 🎯 Conclusion

Both microservices are now configured with their respective databases and have well-structured domain models and repositories. You can:
- Run tests to validate queries and repository logic.
- Use VS Code extensions to inspect tables and documents.
- Extend repositories with custom queries or projections as