---

### 🚀 Coding Smarter with GenAI Assistance

---

### ⚙️ Setting Up Copilot in VS Code

#### 1. **Install the Extension**

* Open VS Code
* Go to Extensions (`Ctrl+Shift+X` or `Cmd+Shift+X`)
* Search for `GitHub Copilot` and install:

  * **GitHub Copilot**
  * (Optional) **GitHub Copilot Chat**

#### 2. **Sign In**

* You’ll be prompted to sign in with your GitHub account
* Accept the permissions
* You must have a GitHub Copilot subscription or trial active

#### 3. **Enable in Java + YAML**

* Copilot works best in supported file types:

  * `.java`, `.yml`, `.json`, `.sql`, `.md`, `.dockerfile`

> ✅ Tip: In settings, ensure `GitHub Copilot: Enable` is checked for all languages.

---

### ✍️ How to Prompt Copilot for Each Component

You can use **natural language comments** to guide Copilot’s output. Here’s a step-by-step set of prompts that mirror this chapter:

#### 📁 Create `Book` Entity

```java
// Create a JPA entity for Book with fields: id, title, isbn, price, quantity, and a ManyToOne relation to Author
```

#### 📁 Create `BookRepository`

```java
// Create a Spring Data JPA repository for Book that supports finding by title, author name, and price range
```

#### 📁 Generate `application.yml` for PostgreSQL

```yaml
# Generate a Spring Boot YAML config to connect to PostgreSQL running on localhost:5432 with username/password
```

#### 📁 MongoDB Document for User

```java
// Create a MongoDB document named User with email, fullName, password, embedded Address and Role list
```

#### 📁 UserRepository for MongoDB

```java
// Create a MongoRepository for User with queries by email and roles.name
```

#### 📁 Seed Sample Data

```java
// Create a CommandLineRunner bean to seed the database with initial books and users
```

#### 📁 SQL DDL for Inventory Tables

```sql
-- Generate SQL DDL to create authors and books tables with foreign keys
```

> ✨ You’ll be amazed how Copilot picks up the intent and generates well-structured, usable code—even test classes!

---

### 📚 Chapter Guide: `Chapter03_Database_and_Repositories.md`

Inside the GitHub repository, we’ve included a detailed Markdown file to guide you (and other developers) through building this chapter step-by-step.

#### 📄 File: `/docs/Chapter03_Database_and_Repositories.md`

Here’s a sample outline of that guide:

```markdown
# Chapter 03 – Setting up Database and Repositories using Spring Data JPA

This guide walks you through implementing Chapter 3 of the Bookstore case study. Each section includes:

✅ Objective  
✅ Code snippet  
✅ VS Code + Copilot prompt (optional)  
✅ Output verification

## Sections

1. Database setup (PostgreSQL + MongoDB via Docker)
2. application.yml configuration
3. JPA entities: Book, Author
4. MongoDB documents: User, Role, Address
5. Repository interfaces (JPA + Mongo)
6. Custom query examples
7. Sample data seeding
8. Testing with @DataJpaTest and @DataMongoTest
9. DDL scripts and insert templates
10. Copilot usage examples
```

This `.md` file will be located inside:

```
📁 bookstore-backend/docs/
└── Chapter03_Database_and_Repositories.md
```

> 📌 Tip: You can link this file directly in your GitHub README or publish it in a GitHub Wiki for learners.

---

### ✅ Summary: Copilot + Markdown = Productive Developers

By using **GitHub Copilot**, you:

* Save time on repetitive boilerplate
* Learn idiomatic Spring practices from AI-generated suggestions
* Stay focused on business logic

---
