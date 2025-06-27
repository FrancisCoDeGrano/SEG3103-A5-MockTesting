# Library Management System - EasyMock Testing Assignment

## 🎯 Project Overview

This project is a **Library Management System** designed specifically for demonstrating EasyMock testing techniques in a team environment. The codebase is intentionally small but feature-rich, providing excellent opportunities for elaborate unit testing with mocked dependencies.


## 🏗️ System Architecture

The system follows a clean architecture pattern with clear separation of concerns:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Service Layer │────│Repository Layer │────│   Model Layer   │
│                 │    │  (Interfaces)   │    │                 │
│ - LibraryService│    │ - BookRepository│    │ - Book          │
│ - Notifications.│    │ - UserRepository│    │ - User          │
│                 │    │ - EmailService  │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Core Components

#### Model Classes
- **Book**: Represents library books with ISBN, title, author, and availability status
- **User**: Represents library users with borrowing limits and current book count

#### Repository Interfaces (IDEAL for Mocking)
- **BookRepository**: Book persistence operations
- **UserRepository**: User persistence operations  
- **EmailService**: Email notification operations

#### Service Classes (Classes Under Test)
- **LibraryService**: Core business logic for borrowing/returning books
- **NotificationService**: Handles batch email notifications for overdue books

## 🛠️ Setup Instructions

### Prerequisites
- Java 11 or higher
- IntelliJ IDEA
- Maven

## 👥 Team Member Assignments

### Team Member 1: LibraryService Borrow Testing
**Class**: `LibraryServiceBorrowTest.java`
**Method Under Test**: `LibraryService.borrowBook(String userId, String isbn)`

**Key Test Scenarios**:
- ✅ Successful borrowing flow
- ❌ User not found
- ❌ User at borrowing limit (3 books max)
- ❌ Book not available
- ❌ Book not found

**EasyMock Features Demonstrated**:
- Multiple mock dependencies (BookRepository, UserRepository, EmailService)
- State verification (book availability, user borrow count)
- Interaction verification (repository saves, email sending)

### Team Member 2: LibraryService Return Testing  
**Class**: `LibraryServiceReturnTest.java`
**Method Under Test**: `LibraryService.returnBook(String userId, String isbn)`

**Key Test Scenarios**:
- ✅ Successful return flow
- ❌ User not found
- ❌ Book not found  
- ❌ Book already available (not borrowed)
- ✅ Last book return (user count goes to 0)

**EasyMock Features Demonstrated**:
- Mock expectation chaining
- State cleanup verification
- Edge case handling
- Email confirmation verification

### Team Member 3: NotificationService Testing
**Class**: `NotificationServiceTest.java`
**Method Under Test**: `NotificationService.sendOverdueNotifications()`

**Key Test Scenarios**:
- ✅ Multiple users with overdue books
- ✅ Single user with overdue books
- ✅ No users with overdue books (empty list)
- ✅ Large user list (batch processing)
- ❌ Repository exception handling
- ❌ Email service exception handling

**EasyMock Features Demonstrated**:
- Collection processing
- Exception simulation with `andThrow()`
- Batch operation verification
- Error handling testing

## 🧪 Key EasyMock Concepts Demonstrated

### Mock Lifecycle
```java
// 1. Create mocks
MockObject mock = EasyMock.createMock(Interface.class);

// 2. Set expectations
EasyMock.expect(mock.method()).andReturn(value);
mock.voidMethod(); EasyMock.expectLastCall();

// 3. Replay mocks
EasyMock.replay(mock);

// 4. Execute code under test
result = serviceUnderTest.methodToTest();

// 5. Verify interactions
EasyMock.verify(mock);
```

### Advanced Features Used
- **Multiple mock management**: Handling 3+ mocks per test
- **Exception simulation**: `andThrow()` for error scenarios  
- **Void method mocking**: `expectLastCall()` for save operations
- **Collection processing**: Testing batch operations
- **State verification**: Checking object state changes

## 🚀 Running the Tests

### Individual Test Classes
```bash
# Team Member 1
mvn test -Dtest=LibraryServiceBorrowTest

# Team Member 2  
mvn test -Dtest=LibraryServiceReturnTest

# Team Member 3
mvn test -Dtest=NotificationServiceTest
```

### All Tests
```bash
mvn test
```

### In IntelliJ
- Right-click on test class → Run
- Right-click on test package → Run All Tests
- Use Ctrl+Shift+F10 to run current test

## 🔗 Additional Resources

- [EasyMock Documentation](http://easymock.org/user-guide.html)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Maven Testing Guide](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)

---

## 📄 License

This project is for educational purposes under the guidelines of the University of Ottawa through the joint program offered by Carleton University.

