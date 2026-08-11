# JPA Quick Summary for Chapter 04

This document summarizes the main Spring Data JPA components used in this chapter.

## 1) Core Components

### Entity
- Purpose: Maps a Java class to a database table.
- Key annotations: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`.
- Example in this chapter: `Book`, `Author`.

### Relationship Mapping
- Purpose: Defines links between entities.
- Common mappings: `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`.
- Example in this chapter: `Book -> Author` with `@ManyToOne` and `Author -> Book` with `@OneToMany`.
- Practical tip: Default to lazy loading for larger object graphs unless eager loading is required.

### Repository
- Purpose: Provides CRUD and query access through interfaces.
- Base interface used here: `JpaRepository<T, ID>`.
- Benefits: Built-in pagination, sorting, and common data access methods.

### Query Methods (Derived Queries)
- Purpose: Build queries from method names, for example `findByTitleContainingIgnoreCase`.
- Benefit: Fast development for straightforward predicates.
- Limitation: Method naming must follow parser rules and can become hard to read for complex filters.

### Custom Queries
- JPQL: Use `@Query("SELECT ...")` for entity-oriented queries.
- Native SQL: Use `@Query(..., nativeQuery = true)` when SQL-level control is needed.
- Tip: Prefer JPQL first for portability, then native SQL only when required.

### Auditing
- Purpose: Tracks create/update timestamps automatically.
- Common setup: `@CreatedDate`, `@LastModifiedDate`, `@EntityListeners`.
- Example in this chapter: `Auditable` base class.

### Transactions
- Purpose: Keeps multi-step data operations consistent.
- Typical usage: `@Transactional` on service methods.
- Best practice: Put transaction boundaries in service layer, not controller layer.

### Pagination and Sorting
- Purpose: Handle large datasets efficiently.
- APIs: `Pageable`, `Sort`, `Page<T>`.
- Benefit: Avoids loading too much data at once and keeps APIs responsive.

## 2) Important Note About Query Derivation

Query derivation is powerful, but it is not "magic" and does not cover every case cleanly.

- Method names must match supported keywords and property paths.
- Nested properties and boolean/null rules can be easy to misread.
- Very long derived method names are harder to maintain and review.
- Some scenarios are clearer with explicit `@Query` methods.

Practical recommendation:
- Use derived queries for simple lookups.
- Switch to `@Query` (JPQL or native SQL) for complex joins, performance tuning, or readability.
- Always validate repository behavior with focused repository tests.

### Quick Decision Table: Derived Query vs `@Query`

| Scenario | Preferred Approach | Why |
| -------- | ------------------ | --- |
| Simple equality or small filters (`findByEmail`, `findByTitleContaining`) | Derived query method | Fast to write, readable, and easy to maintain |
| Nested or lengthy method names becoming hard to read | `@Query` (JPQL) | Makes intent explicit and avoids very long method signatures |
| Complex joins, grouped logic, or tuning for performance | `@Query` (JPQL or native SQL) | Better control over query behavior and execution |
| Database-specific SQL feature is required | `@Query(nativeQuery = true)` | Full SQL control for vendor-specific capabilities |
| Team is unsure about parser behavior or edge cases | `@Query` + repository tests | Reduces ambiguity and verifies behavior explicitly |

## 3) Official References

For full and current behavior details, use the official docs:

- Spring Data JPA project page: https://spring.io/projects/spring-data-jpa
- Spring Data JPA reference documentation: https://docs.spring.io/spring-data/jpa/reference/
- Query methods details (keywords, parsing rules, behavior): https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html
