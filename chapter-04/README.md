# Chapter 04 — Setting Up Databases and Repositories Using Spring Data JPA & MongoDB

This chapter focuses on building the persistence layer for the Bookstore application using **Spring Data JPA** with PostgreSQL and **Spring Data MongoDB** with MongoDB. You'll learn how to configure databases, design JPA entities and MongoDB documents, create repository interfaces, and apply best practices for scalable microservices.

---

## Table of Contents

1. [Chapter Overview](#chapter-overview)
2. [Understanding Database Types](#understanding-database-types)
3. [Building the Inventory Microservice (PostgreSQL + JPA)](#building-the-inventory-microservice-postgresql--jpa)
4. [JPA Entities](#jpa-entities)
5. [Building JPA Repositories](#building-jpa-repositories)
6. [Building the User Microservice (MongoDB + Spring Data MongoDB)](#building-the-user-microservice-mongodb--spring-data-mongodb)
7. [MongoDB Documents](#mongodb-documents)
8. [MongoDB Repository](#mongodb-repository)
9. [Repository Best Practices (JPA vs MongoDB)](#repository-best-practices-jpa-vs-mongodb)
10. [Testing the Repository Layer](#testing-the-repository-layer)
11. [Installation & Setup Steps](#installation--setup-steps)
12. [Resources & References](#resources--references)

---

## Chapter Overview

This chapter provides comprehensive coverage of database persistence strategies for microservices. You'll learn:

- How to choose between relational and NoSQL databases
- Setting up PostgreSQL and MongoDB using Docker
- Creating JPA entities with proper relationships
- Designing MongoDB documents with embedded structures
- Implementing repository patterns for both data stores
- Writing custom queries and derived query methods
- Testing repository layers using Spring Boot test slices
- Best practices for polyglot persistence in microservices

---

## Understanding Database Types

Microservices benefit from **polyglot persistence**, choosing the right database depending on service needs.

### Relational (PostgreSQL)

- Structured tables with strict schema
- Supports joins, normalization, and ACID transactions
- Ideal for Inventory (Books, Authors) with relational data

### NoSQL (MongoDB)

- Flexible schema with JSON-like documents
- Great for evolving and nested structures
- Ideal for User Profiles & Preferences with dynamic data

### Database Comparison

| Feature | PostgreSQL | MongoDB |
|--------|------------|---------|
| Data Model | Tables | Documents |
| Schema | Strict | Flexible |
| Query Language | SQL | BSON |
| Transactions | Full ACID | ACID (with sessions) |
| Best For | Structured, relational data | Dynamic profile-like data |
| Scalability | Vertical + Horizontal | Horizontal |

---

## Building the Inventory Microservice (PostgreSQL + JPA)

### 1. Running PostgreSQL with Docker

```bash
docker run -d \
  --name bookstore-postgres \
  -e POSTGRES_USER=bookstore \
  -e POSTGRES_PASSWORD=bookstore123 \
  -e POSTGRES_DB=inventory \
  -p 5432:5432 \
  postgres:17
```

### 2. Add Required Dependencies

Add to `pom.xml`:

```xml
<!-- Lombok -->
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
</dependency>

<!-- PostgreSQL -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>

<!-- JPA -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### 3. Configure application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory
    username: bookstore
    password: bookstore123

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

---

## JPA Entities

### Book Entity

```java
@Entity
@Table(name = "books")
@Data
public class Book extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String isbn;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;
}
```

### Author Entity

```java
@Entity
@Table(name = "authors")
@Data
public class Author extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Book> books = new ArrayList<>();
}
```

### Auditable Base Class

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class Auditable {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

---

## Building JPA Repositories

### BookRepository

```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Derived query methods
    List<Book> findByTitleContainingIgnoreCase(String keyword);

    List<Book> findByAuthor_Name(String name);

    List<Book> findByPriceBetween(BigDecimal min, BigDecimal max);

    Optional<Book> findByIsbn(String isbn);

    // Custom JPQL query
    @Query("SELECT b FROM Book b WHERE b.quantity < :threshold")
    List<Book> findLowStockBooks(@Param("threshold") int threshold);

    // Native SQL query
    @Query(value = "SELECT * FROM books WHERE price > :minPrice ORDER BY price DESC", 
           nativeQuery = true)
    List<Book> findExpensiveBooks(@Param("minPrice") BigDecimal minPrice);
}
```

### AuthorRepository

```java
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByName(String name);

    List<Author> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Long id);
}
```

---

## Building the User Microservice (MongoDB + Spring Data MongoDB)

### 1. Run MongoDB with Docker

```bash
docker run -d \
  --name bookstore-mongo \
  -e MONGO_INITDB_ROOT_USERNAME=bookstore \
  -e MONGO_INITDB_ROOT_PASSWORD=bookstore123 \
  -e MONGO_INITDB_DATABASE=userDB \
  -p 27017:27017 \
  mongo:8
```

### 2. Add MongoDB Dependency

Add to `pom.xml`:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

### 3. Configure application.yml

```yaml
spring:
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

---

## MongoDB Documents

### User Document

```java
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @Indexed
    private String username;

    private Profile profile;
    private Preferences preferences;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### Embedded Profile Document

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    private String fullName;
    private String phoneNumber;
    private Address address;
}
```

### Embedded Address Document

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

### Embedded Preferences Document

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Preferences {
    private List<String> favoriteGenres;
    private boolean emailNotifications;
    private boolean smsNotifications;
    private String language;
}
```

---

## MongoDB Repository

```java
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Derived query methods
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String keyword);

    // Nested field query
    List<User> findByProfile_Address_City(String city);

    // Custom JSON query
    @Query("{ 'preferences.favoriteGenres': ?0 }")
    List<User> findByFavoriteGenre(String genre);

    // Query with projection
    @Query(value = "{ 'email': ?0 }", fields = "{ 'profile': 1, 'email': 1 }")
    Optional<User> findUserProfileByEmail(String email);
}
```

---

## Repository Best Practices (JPA vs MongoDB)

### Comparison Table

| Practice | JPA | MongoDB |
|---------|-----|----------|
| Single Result | Optional<Book> | Optional<User> |
| Nested Fields | author.name | profile.address.city |
| Custom Queries | @Query JPQL | @Query JSON |
| Pagination | Pageable | Pageable |
| Indexing | DB indexes | @Indexed |
| Lazy Loading | @ManyToOne(LAZY) | N/A (embedded) |
| Transactions | @Transactional | @Transactional |

### Best Practices

#### JPA Best Practices

- Use `Optional<T>` for single results to handle null safely
- Prefer `fetch = FetchType.LAZY` to avoid N+1 queries
- Use `@Query` for complex queries that can't be derived
- Always test with `@DataJpaTest` for repository layer
- Use pagination for large result sets
- Index frequently queried columns

#### MongoDB Best Practices

- Use embedded documents for tightly coupled data
- Apply `@Indexed` to frequently queried fields
- Use projections to fetch only required fields
- Leverage MongoDB's flexible schema for evolving data
- Test with `@DataMongoTest` for repository layer
- Consider compound indexes for complex queries

---

## Testing the Repository Layer

### JPA Repository Testing

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void testFindAll() {
        // Given
        Author author = new Author();
        author.setName("John Doe");
        authorRepository.save(author);

        Book book = new Book();
        book.setTitle("Test Book");
        book.setIsbn("1234567890");
        book.setPrice(new BigDecimal("29.99"));
        book.setQuantity(10);
        book.setAuthor(author);
        bookRepository.save(book);

        // When
        List<Book> books = bookRepository.findAll();

        // Then
        assertThat(books).isNotEmpty();
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindByTitleContaining() {
        // Test implementation
    }

    @Test
    void testFindLowStockBooks() {
        // Test implementation
    }
}
```

### MongoDB Repository Testing

```java
@DataMongoTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        // Given
        User user = User.builder()
            .email("test@example.com")
            .username("testuser")
            .profile(Profile.builder()
                .fullName("Test User")
                .build())
            .build();
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void testFindByFavoriteGenre() {
        // Test implementation
    }

    @Test
    void testFindByCity() {
        // Test implementation
    }
}
```

---

## Installation & Setup Steps

### 1. Install Docker

Download from: <https://www.docker.com/products/docker-desktop>

### 2. Start PostgreSQL Container

```bash
docker run -d \
  --name bookstore-postgres \
  -e POSTGRES_USER=bookstore \
  -e POSTGRES_PASSWORD=bookstore123 \
  -e POSTGRES_DB=inventory \
  -p 5432:5432 \
  postgres:17
```

### 3. Start MongoDB Container

```bash
docker run -d \
  --name bookstore-mongo \
  -e MONGO_INITDB_ROOT_USERNAME=bookstore \
  -e MONGO_INITDB_ROOT_PASSWORD=bookstore123 \
  -e MONGO_INITDB_DATABASE=userDB \
  -p 27017:27017 \
  mongo:8
```

### 4. Verify Containers are Running

```bash
docker ps
```

### 5. Add Dependencies to Maven Projects

Add JPA dependencies to Inventory microservice and MongoDB dependencies to User microservice as shown in the sections above.

### 6. Run the Applications

```bash
# Inventory microservice
cd inventory-service
./mvnw spring-boot:run

# User microservice
cd user-service
./mvnw spring-boot:run
```

---

## Resources & References

- **Spring Data JPA Documentation**: <https://spring.io/projects/spring-data-jpa>
- **Spring Data MongoDB Documentation**: <https://spring.io/projects/spring-data-mongodb>
- **PostgreSQL Documentation**: <https://www.postgresql.org/docs/>
- **MongoDB Documentation**: <https://docs.mongodb.com/>
- **Docker Documentation**: <https://docs.docker.com/>
- **Lombok Documentation**: <https://projectlombok.org/>

---