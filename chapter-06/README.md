# Chapter Guide: Understanding and Developing the Codebase with AI Assistance

## Overview

This chapter demonstrates a microservices-based backend using Spring Boot. The workspace contains two main services:

- `inventory-ms`: Manages inventory-related operations.
- `user-ms`: Handles user management.

Each service is structured as a standard Spring Boot project, with clear separation of concerns (controllers, services, repositories, DTOs, etc.).

## Project Structure

```
Spring-Boot-and-Angular-2E_Backend/
├── inventory-ms/
│   ├── src/main/java/com/packt/bookstore/inventory/
│   └── ...
├── user-ms/
│   ├── src/main/java/com/packt/bookstore/users/
│   └── ...
```

## How to Run the Code

1. **Open the Workspace in VS Code**
   - Use the built-in terminal or the provided `mvnw` scripts to build and run each service:
     ```sh
     cd inventory-ms
     ./mvnw spring-boot:run
     # In a new terminal:
     cd ../user-ms
     ./mvnw spring-boot:run
     ```

2. **Configuration**
   - Each service has its own `application.yml` for configuration.
   - Test configurations are in `src/test/resources/application-test.yml`.

3. **Testing**
   - Run tests with:
     ```sh
     ./mvnw test
     ```

## Using GitHub Copilot and AI Agents in VS Code

### 1. Enabling Copilot
- Install the [GitHub Copilot extension](https://marketplace.visualstudio.com/items?itemName=GitHub.copilot) in VS Code.
- Sign in with your GitHub account.

### 2. Writing and Refactoring Code
- Start typing in Java files; Copilot will suggest code completions and improvements.
- Use comments to describe what you want to implement, and Copilot will generate code snippets.
- Accept suggestions with `Tab` or cycle through alternatives.

### 3. Using Copilot Chat and Agents
- Open the Copilot Chat panel (`Cmd+I` or from the sidebar).
- Ask questions about the codebase, request explanations, or generate new code (e.g., "Add a REST endpoint for books").
- Use Copilot Agents to:
  - Refactor code automatically.
  - Generate tests for your classes.
  - Get step-by-step guidance for debugging or implementing features.

### 4. Best Practices
- Review all AI-generated code for correctness and security.
- Use Copilot as a pair programmer: ask for explanations, code reviews, or alternative implementations.
- Leverage Copilot to speed up repetitive tasks, boilerplate code, and documentation.

## Chapter Flow

1. **Explore the code structure**: Understand how controllers, services, and repositories interact.
2. **Run and test each microservice**: Ensure both services start and communicate as expected.
3. **Use Copilot to extend functionality**: Try adding new endpoints, DTOs, or tests with Copilot’s help.
4. **Iterate and Refine**: Use Copilot’s suggestions to refactor and improve code quality.

---

By following this guide and leveraging AI tools like GitHub Copilot, you can efficiently understand, develop, and extend the chapter’s codebase.
