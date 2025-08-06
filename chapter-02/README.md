
# Chapter 2: Getting Started with Microservices using Spring Boot

Welcome to Chapter 2 of the **Spring Boot and Angular** book! This chapter introduces you to building microservices with Spring Boot.

## 📋 Chapter Overview

In this chapter, you will learn:

* **Microservices Architecture Fundamentals**: Understanding the core concepts and benefits of microservices
* **Spring Boot Project Setup**: Creating a basic Spring Boot microservice from scratch
* **Project Structure**: Organizing your microservice project following best practices
* **Maven Configuration**: Setting up dependencies and build configuration
* **Application Bootstrap**: Creating the main application class and basic configuration

## 🏗️ Project Structure

This chapter contains:

```text
chapter-02/
├── README.md                 # This guide
├── base source code/         # Initial project templates
├── inventory-ms/            # Inventory Microservice Project
    ├── pom.xml              # Maven configuration
    ├── src/
    │   ├── main/java/       # Application source code
    │   └── test/java/       # Test source code
    └── target/              # Build artifacts
```

## 🎯 Learning Objectives

By the end of this chapter, you will be able to:

* Set up a new Spring Boot microservice project
* Understand the basic structure of a Spring Boot application
* Configure Maven dependencies for microservices development
* Run and test a basic Spring Boot application
* Apply microservices design principles

## 🚀 Getting Started

### Prerequisites

* Java 24 or higher
* Maven 3.8+
* IDE (VS Code)
* Basic understanding of Java and Spring Framework

### Running the Project

1. Navigate to the `inventory-ms` directory
2. Run the following commands:

```bash
# Build the project
./mvnw clean compile

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

The application will start on `http://localhost:8080`

---

## **Accelerating Development with GitHub Copilot and VS Code**

### 🚀 Coding Smarter with GenAI Assistance

While building backend systems with Spring Boot can feel empowering, there are still moments when we all stare at a blank editor, unsure of syntax, best practices, or even what to name a method. That's where **GitHub Copilot** shines.

In this section, you'll learn how to:

* Set up GitHub Copilot in VS Code
* Prompt Copilot to generate the source code covered in this chapter
* Use GitHub Copilot to accelerate microservice development
* Apply AI-assisted coding best practices

### 🧠 What Is GitHub Copilot?

GitHub Copilot is an AI pair programmer developed by GitHub and OpenAI. It uses natural language context and code surroundings to suggest:

* Method signatures and implementations
* Spring Boot configurations and annotations
* Maven dependency management
* Application properties and YAML configurations
* Basic microservice structure and boilerplate code

### Using Copilot for Spring Boot Development

GitHub Copilot can assist you in:

* Creating Spring Boot starter projects
* Generating boilerplate code for controllers, services, and repositories
* Suggesting appropriate Spring annotations
* Writing configuration files
* Creating unit and integration tests

### ✅ Summary: Copilot + Spring Boot = Productive Developers

By using **GitHub Copilot**, you:

* Save time on repetitive boilerplate code
* Learn idiomatic Spring Boot practices from AI-generated suggestions
* Stay focused on business logic rather than syntax
* Accelerate microservice development workflows

## 📚 Key Concepts Covered

### 1. Microservices Architecture

* Service independence and autonomy
* Scalability and maintainability benefits
* Communication patterns between services

### 2. Spring Boot Fundamentals

* Auto-configuration capabilities
* Embedded server deployment
* Starter dependencies for rapid development

### 3. Project Organization

* Maven project structure
* Package organization best practices
* Configuration management

## ✅ Chapter Summary

In this chapter, you've learned the foundational concepts of microservices development with Spring Boot. The `inventory-ms` project serves as a starting point for building more complex microservices in subsequent chapters.

**Next Steps**: Chapter 3 will build upon this foundation by adding database integration and repositories using Spring Data JPA.

---
