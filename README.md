[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/V8U2uMSg)
[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=24274097&assignment_repo_type=AssignmentRepo)
# Library Borrowing System

> A console-based library management system written in Java.
> A **Librarian** manages books, multimedia, and members,
> while **Members** can borrow, return, and search for items.
> Demonstrates **Inheritance**, **Overriding**, **Overloading**, and **Encapsulation**.

---

## Table of Contents

- [Screenshot](#screenshot)
- [OOP Concepts](#oop-concepts)
- [Features](#features)
- [Project Structure](#project-structure)
- [Class Hierarchy](#class-hierarchy)
- [Class Overview](#class-overview)
- [Class Associations](#class-associations)
- [Initial Data](#initial-data)
- [How to Run](#how-to-run)
- [Menu Options](#menu-options)
- [Package](#package)

---

## Screenshot

![App Screenshot](App%20Screenshot.png)

---

## OOP Concepts

| Concept       | Where It Is Applied                                                                                   |
|---------------|-------------------------------------------------------------------------------------------------------|
| Inheritance   | `Books` and `Multimedia` extend `LibraryItem`; `Member` and `Librarian` extend `Person`              |
| Overriding    | `getInfo()` and `toString()` overridden in `Books`, `Multimedia`, `Member`, `Librarian`              |
| Overloading   | `addBook()` (with/without genre), `addMultimedia()` (with/without duration), `searchItem()` (with/without keyword) |
| Encapsulation | All attributes are `private` or `protected`, accessed only via getters and setters                   |

---

## Features

### Book Management (Librarian)

- Add a new book with or without a genre (method overloading)
- Remove a book from the catalog
- Update a book's title, author, or genre
- View all books with their availability status

### Multimedia Management (Librarian)

- Add a new multimedia item with or without a duration (method overloading)
- Remove a multimedia item
- Update a multimedia item's title, type, or duration
- View all multimedia items

### Member Management (Librarian)

- Register a new member
- Remove a member
- Update a member's name
- View all registered members

### Borrow & Return (Member)

- Borrow any library item — book or multimedia (up to 5 at a time)
- Return a borrowed item
- Search for an item by title keyword (method overloading — with keyword)
- Browse all available items (method overloading — without keyword)

### Records

- View all borrow and return transactions

---

## Project Structure

```text
LibraryBorrowingSystem/
├── src/
│   └── LibraryBorrowingSystem/       # Package folder (matches package declaration)
│       ├── Person.java               # Base class: id and name — extended by Member and Librarian
│       ├── LibraryItem.java          # Base class: itemId, title, available — extended by Books and Multimedia
│       ├── Books.java                # Subclass of LibraryItem; adds author and genre
│       ├── Multimedia.java           # Subclass of LibraryItem; adds type and duration
│       ├── Member.java               # Subclass of Person; can borrow, return, and search items
│       ├── BorrowRecord.java         # Records a single borrow/return transaction
│       ├── Librarian.java            # Subclass of Person; manages catalog, members, and records
│       └── main.java                 # Entry point; runs the 18-option menu-driven interface
├── out/                              # Compiled .class files (gitignored)
├── App Screenshot.png                # Application screenshot
├── .gitignore
└── README.md
```

---

## Class Hierarchy

```text
Person
├── Member        (extends Person)
└── Librarian     (extends Person)

LibraryItem
├── Books         (extends LibraryItem)
└── Multimedia    (extends LibraryItem)

BorrowRecord      (standalone — links Member and LibraryItem)
```

---

## Class Overview

### `Person` *(base class)*

Base class for all people in the system.

| Attribute | Type   | Access    | Description                        |
|-----------|--------|-----------|------------------------------------|
| id        | String | protected | Unique identifier (e.g. L001)      |
| name      | String | protected | Full name of the person            |

Key methods: `getInfo()`, `toString()`, `getId()`, `getName()`

---

### `LibraryItem` *(base class)*

Base class for all borrowable items in the library.

| Attribute | Type    | Access    | Description                             |
|-----------|---------|-----------|-----------------------------------------|
| itemId    | String  | protected | Unique item identifier (e.g. B001)      |
| title     | String  | protected | Title of the item                       |
| available | boolean | protected | True if the item is available to borrow |

Key methods: `getInfo()`, `toString()`, `getItemId()`, `getTitle()`, `isAvailable()`

---

### `Books` *(extends LibraryItem)*

Represents a book in the library collection.

| Attribute | Type   | Access  | Description                        |
|-----------|--------|---------|------------------------------------|
| author    | String | private | Author of the book                 |
| genre     | String | private | Genre/category (e.g. Classic, Fiction, Sci-Fi) |

*Inherits:* `itemId`, `title`, `available` from `LibraryItem`

Key methods: `getInitialBooks()`, `getInfo()` *(overrides LibraryItem)*, `toString()` *(overrides LibraryItem)*

---

### `Multimedia` *(extends LibraryItem)*

Represents a multimedia item (DVD, CD, Audiobook) in the library.

| Attribute | Type   | Access  | Description                            |
|-----------|--------|---------|----------------------------------------|
| type      | String | private | Media type (DVD / CD / Audiobook)      |
| duration  | String | private | Duration of the item (e.g. "148 min")  |

*Inherits:* `itemId`, `title`, `available` from `LibraryItem`

Key methods: `getInitialMultimedia()`, `getInfo()` *(overrides LibraryItem)*, `toString()` *(overrides LibraryItem)*

---

### `Member` *(extends Person)*

Represents a library member who can borrow and return items.

| Attribute     | Type           | Access  | Description                           |
|---------------|----------------|---------|---------------------------------------|
| borrowedItems | LibraryItem[]  | private | Array of currently borrowed items     |
| borrowCount   | int            | private | Number of items currently borrowed    |

*Inherits:* `id`, `name` from `Person`

Key methods: `getInitialMembers()`, `borrowItem()`, `returnItem()`,
`searchItem(catalog, size, keyword)` *(overloaded — with keyword)*,
`searchItem(catalog, size)` *(overloaded — without keyword)*,
`getInfo()` *(overrides Person)*, `toString()` *(overrides Person)*

---

### `BorrowRecord`

Tracks a single borrow or return transaction.

| Attribute  | Type        | Access  | Description                              |
|------------|-------------|---------|------------------------------------------|
| recordId   | String      | private | Unique record ID (e.g. REC001)           |
| member     | Member      | private | The member who borrowed the item         |
| item       | LibraryItem | private | The item that was borrowed               |
| borrowDate | String      | private | Date the item was borrowed               |
| returnDate | String      | private | Date the item was returned ("-" if not)  |
| returned   | boolean     | private | True if the item has been returned       |

---

### `Librarian` *(extends Person)*

Manages the library catalog, members, and all borrow records.

| Attribute     | Type           | Access  | Description                             |
|---------------|----------------|---------|-----------------------------------------|
| catalog       | Books[]        | private | Array of all books in the system        |
| multimedia    | Multimedia[]   | private | Array of all multimedia in the system   |
| members       | Member[]       | private | Array of all registered members         |
| borrowRecords | BorrowRecord[] | private | Array of all borrow transactions        |

*Inherits:* `id`, `name` from `Person`

Key methods:
`addBook(id, title, author, genre)` *(overloaded — with genre)*,
`addBook(id, title, author)` *(overloaded — defaults genre to "General")*,
`addMultimedia(id, title, type, duration)` *(overloaded — with duration)*,
`addMultimedia(id, title, type)` *(overloaded — defaults duration to "Unknown")*,
`removeBook()`, `updateBook()`, `displayCatalog()`,
`removeMultimedia()`, `updateMultimedia()`, `displayMultimedia()`,
`registerMember()`, `removeMember()`, `updateMember()`, `displayMembers()`,
`findItemById()`, `getAllItems()`, `recordBorrow()`, `recordReturn()`, `displayAllRecords()`,
`getInfo()` *(overrides Person)*, `toString()` *(overrides Person)*

---

## Class Associations

```text
Person
├── Member    (inheritance: Member extends Person)
└── Librarian (inheritance: Librarian extends Person)

LibraryItem
├── Books     (inheritance: Books extends LibraryItem)
└── Multimedia(inheritance: Multimedia extends LibraryItem)

Librarian ──manages──> Books[]
Librarian ──manages──> Multimedia[]
Librarian ──manages──> Member[]
Librarian ──manages──> BorrowRecord[]

BorrowRecord ──links──> Member
BorrowRecord ──links──> LibraryItem  (can be Books or Multimedia)

Member ──borrows──> LibraryItem[]    (can be Books or Multimedia)
```

---

## Initial Data

The system starts with pre-loaded data defined inside each class:

**Books** — defined in `Books.getInitialBooks()`

| ID   | Title                  | Author              | Genre     |
|------|------------------------|---------------------|-----------|
| B001 | The Great Gatsby       | F. Scott Fitzgerald | Classic   |
| B002 | To Kill a Mockingbird  | Harper Lee          | Fiction   |
| B003 | 1984                   | George Orwell       | Dystopian |
| B004 | Brave New World        | Aldous Huxley       | Sci-Fi    |
| B005 | The Catcher in the Rye | J.D. Salinger       | Fiction   |

**Multimedia** — defined in `Multimedia.getInitialMultimedia()`

| ID    | Title              | Type      | Duration |
|-------|--------------------|-----------|----------|
| MM001 | Inception          | DVD       | 148 min  |
| MM002 | Dark Side of Moon  | CD        | 43 min   |
| MM003 | Sapiens Audiobook  | Audiobook | 15 hrs   |

**Members** — defined in `Member.getInitialMembers()`

| ID   | Name    |
|------|---------|
| M001 | Alice   |
| M002 | Bob     |
| M003 | Charlie |

---

## How to Run

### Requirements

- Java JDK 8 or higher

### Compile

```bash
cd /path/to/LibraryBorrowingSystem
javac -d out src/LibraryBorrowingSystem/*.java
```

### Run

```bash
java -cp out LibraryBorrowingSystem.main
```

---

## Menu Options

### Level 1 — Main Menu

```text
============================================
          LIBRARY BORROWING SYSTEM
============================================
  1. Book Management
  2. Multimedia Management
  3. Member Management
  4. Borrow & Return
--------------------------------------------
  0. Exit
============================================
```

> Exit (0) is only available here. Each option opens a sub-menu.

---

### Level 2 — Sub-menus

**Book Management**
```text
--------------------------------------------
          BOOK MANAGEMENT
--------------------------------------------
  1. Add Book (with Genre)
  2. Add Book (default Genre: General)
  3. Remove Book
  4. Update Book
  5. View All Books
--------------------------------------------
  0. Back
--------------------------------------------
```

**Multimedia Management**
```text
--------------------------------------------
          MULTIMEDIA MANAGEMENT
--------------------------------------------
  1. Add Multimedia (with Duration)
  2. Add Multimedia (default Duration: Unknown)
  3. Remove Multimedia
  4. Update Multimedia
  5. View All Multimedia
--------------------------------------------
  0. Back
--------------------------------------------
```

**Member Management**
```text
--------------------------------------------
          MEMBER MANAGEMENT
--------------------------------------------
  1. Register Member
  2. Remove Member
  3. Update Member Name
  4. View All Members
--------------------------------------------
  0. Back
--------------------------------------------
```

**Borrow & Return**
```text
--------------------------------------------
          BORROW & RETURN
--------------------------------------------
  1. Borrow Item
  2. Return Item
  3. Search Item by Keyword
  4. Browse All Available Items
  5. View All Borrow Records
--------------------------------------------
  0. Back
--------------------------------------------
```

---

## Package

All classes are organized under the `LibraryBorrowingSystem` package.

```java
package LibraryBorrowingSystem;
```
