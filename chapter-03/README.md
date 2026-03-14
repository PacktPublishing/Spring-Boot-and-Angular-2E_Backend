# Chapter 03 — Setting Up Your AI Development Assistant

AI assistants have become an essential part of the modern development workflow. In this chapter, we explore how tools like GitHub Copilot, Cursor, and Claude enhance your productivity by generating code, refactoring safely, helping with debugging, and accelerating both Java Spring Boot and Angular development.

This README provides a structured reference for everything covered in the chapter, including comparisons of the tools, configuration steps, code examples, and best practices.

---

## Table of Contents

- [Chapter 03 — Setting Up Your AI Development Assistant](#chapter-03--setting-up-your-ai-development-assistant)
  - [Table of Contents](#table-of-contents)
  - [Chapter Overview](#chapter-overview)
  - [✅ Before You Run This Chapter](#-before-you-run-this-chapter)
  - [📦 Chapter Source Code Availability](#-chapter-source-code-availability)
  - [The Rise of AI Development Assistants](#the-rise-of-ai-development-assistants)
    - [How AI Accelerates Development](#how-ai-accelerates-development)
  - [Comparing Leading Tools: Cursor, Claude \& GitHub Copilot](#comparing-leading-tools-cursor-claude--github-copilot)
    - [Quick Comparison Table](#quick-comparison-table)
  - [Why GitHub Copilot Works Best with VS Code](#why-github-copilot-works-best-with-vs-code)
  - [Installing \& Configuring GitHub Copilot in VS Code](#installing--configuring-github-copilot-in-vs-code)
    - [1. Install Copilot Extensions](#1-install-copilot-extensions)
    - [2. Authenticate](#2-authenticate)
    - [3. Enable Key Features](#3-enable-key-features)
  - [Using GitHub Copilot in Spring Boot \& Angular Projects](#using-github-copilot-in-spring-boot--angular-projects)
    - [Example: Generate a full REST Endpoint](#example-generate-a-full-rest-endpoint)
    - [Example: Create Repository Methods](#example-create-repository-methods)
    - [Example: Generate Angular Service](#example-generate-angular-service)
  - [Prompt Engineering for Developers](#prompt-engineering-for-developers)
    - [Example Prompt Patterns](#example-prompt-patterns)
  - [Best Practices for AI Pair Programming](#best-practices-for-ai-pair-programming)
    - [✔ DO](#-do)
    - [❌ AVOID](#-avoid)
  - [Limitations \& Future Directions of GenAI](#limitations--future-directions-of-genai)
  - [Installation \& Setup Steps](#installation--setup-steps)
    - [1. Install VS Code](#1-install-vs-code)
    - [2. Install GitHub Copilot](#2-install-github-copilot)
    - [3. Install Java Extensions](#3-install-java-extensions)
    - [4. Install Angular Tools](#4-install-angular-tools)
    - [5. Clone the Bookstore Repositories](#5-clone-the-bookstore-repositories)
  - [Resources \& References](#resources--references)

---

## Chapter Overview

This chapter introduces the modern GenAI toolset that supports you while building the Bookstore application. You'll learn:

- How AI assistants evolved
- The strengths and limitations of Cursor, Claude, and Copilot
- Why VS Code + Copilot is the most productive environment
- How to configure Copilot properly
- How to use Copilot to write controllers, repositories, DTOs, and Angular services
- Essential prompt engineering patterns
- Best practices & pitfalls when pairing with AI

---

## ✅ Before You Run This Chapter

Please confirm the required runtime dependencies before running this chapter:

- Confirm the database is started (if this chapter uses one).
- Confirm any infrastructure dependencies are running (for example Docker services, if used).
- Confirm any dependencies from previous chapters are running as needed for your flow.

---

## 📦 Chapter Source Code Availability

The final source code for this chapter is already uploaded in this directory.

Use this folder as the reference implementation for the completed chapter state.

---

## The Rise of AI Development Assistants

AI coding tools have transformed development from simple autocomplete to full contextual assistance.

### How AI Accelerates Development

- Generates boilerplate code (DTOs, controllers, services)
- Suggests tests (JUnit, Mockito)
- Explains unfamiliar Spring or Angular APIs
- Helps onboard faster into large codebases
- Improves readability and code quality
- Bridges backend–frontend workflows

---

## Comparing Leading Tools: Cursor, Claude & GitHub Copilot

### Quick Comparison Table

| Feature / Use Case     | Cursor IDE | Claude         | GitHub Copilot    |
| ---------------------- | ---------- | -------------- | ----------------- |
| Project-wide context   | ⭐⭐⭐⭐   | ⭐⭐⭐⭐⭐     | ⭐⭐⭐            |
| Inline code generation | ⭐⭐⭐⭐⭐ | Not supported  | ⭐⭐⭐⭐⭐        |
| Refactoring support    | Strong     | Insightful     | Suggestive        |
| Architecture reasoning | Good       | Excellent      | Moderate          |
| IDE Integration        | Cursor     | Standalone app | VS Code, IntelliJ |

---

## Why GitHub Copilot Works Best with VS Code

Copilot is optimized for VS Code because:

- Native inline completion ("ghost text")
- Strong integration with Java, Spring, Angular extensions
- Access to multi-file project context
- Smooth Copilot Chat sidebar
- Works perfectly with Spring Boot project structure (pom.xml, controllers, repos)

---

## Installing & Configuring GitHub Copilot in VS Code

### 1. Install Copilot Extensions

In VS Code:

```
Ctrl+Shift+X → Search "GitHub Copilot"
Ctrl+Shift+X → Search "GitHub Copilot Chat"
```

Install both official extensions.

### 2. Authenticate

```
Ctrl+Shift+P → Copilot: Sign In
```

Login through GitHub in your browser.

### 3. Enable Key Features

From VS Code Settings:

- ✔ Enable Inline Suggestions
- ✔ Enable Copilot Chat
- ✔ Enable for all file types (.java, .ts, .html, .yaml, .md…)

---

## Using GitHub Copilot in Spring Boot & Angular Projects

### Example: Generate a full REST Endpoint

```java
// Get all books
@GetMapping
public ResponseEntity<List<BookDTO>> getAllBooks() {
    return ResponseEntity.ok(bookService.getAllBooks());
}
```

### Example: Create Repository Methods

```java
// Find books by author (case-insensitive)
List<Book> findByAuthorIgnoreCase(String author);
```

### Example: Generate Angular Service

```typescript
// Get books from backend
getBooks(): Observable<Book[]> {
  return this.http.get<Book[]>(`${this.baseUrl}/books`);
}
```

---

## Prompt Engineering for Developers

### Example Prompt Patterns

**REST Controller prompt:**

```java
// Create a GET endpoint to fetch all books
```

**Service method prompt:**

```java
// Add a method to find books by category and price range
```

**Repository prompt:**

```java
// Spring Data method to find books by title (case insensitive)
```

**Unit Test prompt:**

```java
// Write a test for getBooksByAuthor endpoint
```

---

## Best Practices for AI Pair Programming

### ✔ DO

- Be specific with prompts
- Use comments inside code files
- Validate suggestions before accepting
- Request multiple suggestions when needed
- Let AI scaffold repetitive code

### ❌ AVOID

- Blindly trusting generated code
- Overloaded prompts ("make it secure, fast, scalable, clean…")
- Large unclear requests

---

## Limitations & Future Directions of GenAI

- Occasional hallucinations
- Limited context window (~150 lines)
- May produce outdated API calls
- Needs human validation
- Will improve with larger context & repository-wide reasoning

---

## Installation & Setup Steps

### 1. Install VS Code

<https://code.visualstudio.com>

### 2. Install GitHub Copilot

<https://github.com/features/copilot>

### 3. Install Java Extensions

- Extension Pack for Java
- Spring Boot Tools

### 4. Install Angular Tools

- Angular Essentials
- TypeScript + Web Tools

### 5. Clone the Bookstore Repositories

```bash
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Frontend
```

---

## Resources & References

- **GitHub Copilot Documentation**: <https://docs.github.com/en/copilot>
- **VS Code Documentation**: <https://code.visualstudio.com/docs>
- **Cursor IDE**: <https://cursor.com>
- **Claude AI**: <https://claude.ai>

---
