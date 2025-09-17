# Database Inspection Guide – PostgreSQL & MongoDB

This guide walks you through inspecting the databases used in the Bookstore microservices with **VS Code extensions**, so you can confirm tables, collections, and documents are created and populated correctly.

---

## 🗃️ Inspecting PostgreSQL (Inventory Microservice)

The **Inventory microservice** uses PostgreSQL for relational data (`Book`, `Author`).

### Step 1: Install the PostgreSQL Extension
1. Open VS Code.
2. Press `Ctrl+Shift+X` (or `Cmd+Shift+X` on Mac).
3. Search for **PostgreSQL**.
4. Install **PostgreSQL (by Microsoft)**.

### Step 2: Connect to the Database
1. Open the Command Palette (`Ctrl+Shift+P`).
2. Select **PostgreSQL: New Connection**.
3. Enter:
   - Host: `localhost`
   - Port: `5432`
   - Database: `inventory`
   - Username: `bookstore`
   - Password: `bookstore123`

### Step 3: Explore Tables
In the VS Code sidebar, expand the `inventory` database. You should see:
- `books` table
- `authors` table

Run queries:
```sql
SELECT * FROM books;
SELECT * FROM authors;
```

✅ You should see the test data created in repository tests or seeders.

---

## 🍃 Inspecting MongoDB (User Management Microservice)

The **User Management microservice** uses MongoDB for document storage (`User`, `Profile`, `Role`, etc.).

### Step 1: Install MongoDB Extension
1. In VS Code, press `Ctrl+Shift+X`.
2. Search for **MongoDB for VS Code**.
3. Install the extension by MongoDB Inc.

### Step 2: Connect to MongoDB
1. Click the **MongoDB leaf icon** in the Activity Bar.
2. Select **Connect**.
3. Use the URI:
```
mongodb://localhost:27017
```

4. Select the `users` database.

### Step 3: Inspect Collections
Open the `users` collection. You should see documents like:
```json
{
  "_id": "user_001",
  "email": "admin@example.com",
  "username": "admin",
  "status": "ACTIVE",
  "roles": [
    { "name": "ADMIN", "description": "Admin Role", "permissions": ["MANAGE_USERS"] }
  ],
  "profile": {
    "fullName": "Admin User",
    "phone": "+1-555-1234",
    "address": { "city": "New York", "postalCode": "10001" }
  },
  "preferences": { "language": "en", "newsletter": true, "currency": "USD" },
  "createdAt": "2025-07-28T08:00:00Z"
}
```

✅ This confirms embedded fields (`profile`, `roles`, `preferences`) are stored properly.

---

## 🎯 Conclusion

With these VS Code extensions:
- You can quickly connect to PostgreSQL and confirm your `Book` and `Author` entities are persisted as rows in relational tables.
- You can inspect MongoDB documents to verify that embedded data structures like `Profile`, `Address`, and `Roles` are stored correctly.

This complements your automated repository tests by giving you **direct visibility into the data layer** — a critical step when debugging, validating mappings, or just exploring your application’s state.

---
