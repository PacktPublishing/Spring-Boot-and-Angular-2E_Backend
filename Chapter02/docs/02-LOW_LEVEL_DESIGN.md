
2. **docs/LOW_LEVEL_DESIGN.md**
```markdown
# Bookstore Application - Low-Level Design

## Book Inventory Microservice

### Class Diagram
```plantuml
@startuml
!theme plain

class Book {
  - id: Long
  - title: String
  - author: Author
  - genre: Genre
  - price: BigDecimal
  - stock: int
}

class Author {
  - id: Long
  - name: String
  - bio: String
}

class Genre {
  - id: Long
  - name: String
}

class Inventory {
  - bookId: Long
  - stockLevel: int
}

Book "1" --> "1" Author
Book "1" --> "1" Genre
Book "1" --> "1" Inventory
@enduml


