# Developer Guide – Using GitHub Copilot with VS Code

This guide explains how to set up and use **GitHub Copilot** in VS Code as your AI-powered coding assistant while working on the Bookstore microservices.

---

## 🚀 Why Use Copilot?
GitHub Copilot helps speed up development by:
- Suggesting code snippets as you type.
- Autocompleting repository queries and entity mappings.
- Generating boilerplate classes (e.g., DTOs, Services, Controllers).
- Assisting with test cases (e.g., `@DataJpaTest`, `@DataMongoTest`, and `@SpringBootTest`).

---

## ⚙️ Step 1: Install GitHub Copilot Extension
1. Open VS Code.
2. Press `Ctrl+Shift+X` (or `Cmd+Shift+X` on Mac) to open the Extensions panel.
3. Search for **GitHub Copilot**.
4. Install it and sign in with your GitHub account.

---

## 🧩 Step 2: Enable Copilot in Your Project
1. Open the Command Palette (`Ctrl+Shift+P`).
2. Search for **Copilot: Enable**.
3. Ensure suggestions are active in Java, YAML, and test files.

---

## ✍️ Step 3: Practical Use Cases

### 1. Generate Entities and Documents
When creating a new JPA entity or MongoDB document, start typing:
```java
@Entity
public class Book {
```
Copilot will suggest fields, constructors, and even relationships based on context.

### 2. Repository Methods
For `BookRepository`, typing `List<Book> findBy` will prompt Copilot to suggest derived query methods like `findByTitleContainingIgnoreCase`.

### 3. Test Classes
When you create a test class with `@DataJpaTest` or `@DataMongoTest`, Copilot can suggest test methods to validate repository queries.

### 4. application.yml
Copilot can help generate boilerplate database configurations when you start typing `spring:`.

---

## 🔍 Step 4: Best Practices
- **Accept suggestions wisely** – review and adapt Copilot’s code.
- **Leverage comments** – write `// create repository for User with preferences language filter` and Copilot will generate relevant methods.
- **Use it for repetitive code** – DTOs, getters/setters, or standard repository queries.

---

## 🎯 Conclusion
GitHub Copilot acts like a helpful pair programmer in VS Code. For this project, it will:
- Speed up writing boilerplate for **Inventory (PostgreSQL)** and **User Management (MongoDB)** microservices.
- Reduce time spent on repetitive configuration and repository methods.
- Help generate clean and consistent tests for both JPA and MongoDB layers.

With Copilot integrated, you can focus more on business logic and architectural decisions while letting the assistant handle common coding patterns.

---
