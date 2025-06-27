package com.library.service;

import com.library.model.Book;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Elaborate test case for ANDRES
 * Testing LibraryService.returnBook() method
 */
public class LibraryServiceReturnTest {

    private LibraryService libraryService;
    private BookRepository mockBookRepository;
    private UserRepository mockUserRepository;
    private EmailService mockEmailService;

    // Test data
    private User userWithBorrowedBooks;
    private User userWithNoBooks;
    private Book borrowedBook;
    private Book alreadyAvailableBook;

    @Before
    public void setUp() {
        // Create mocks
        mockBookRepository = EasyMock.createMock(BookRepository.class);
        mockUserRepository = EasyMock.createMock(UserRepository.class);
        mockEmailService = EasyMock.createMock(EmailService.class);

        // Create a service with injected mocks
        libraryService = new LibraryService(mockBookRepository, mockUserRepository, mockEmailService);

        // Set up test data
        userWithBorrowedBooks = new User("U001", "Alice Johnson", "alice@example.com");
        userWithBorrowedBooks.setBorrowedBooksCount(2);

        userWithNoBooks = new User("U002", "Bob Wilson", "bob@example.com");
        userWithNoBooks.setBorrowedBooksCount(0);

        borrowedBook = new Book("978-1111111111", "Currently Borrowed", "Some Author");
        borrowedBook.setAvailable(false); // Book is currently borrowed

        alreadyAvailableBook = new Book("978-2222222222", "Available Book", "Another Author");
        alreadyAvailableBook.setAvailable(true); // Book is already available
    }

    @Test
    public void testReturnBook_SuccessfulReturn() {
        // Arrange - Set up mock expectations
        EasyMock.expect(mockUserRepository.findById("U001")).andReturn(userWithBorrowedBooks);
        EasyMock.expect(mockBookRepository.findByIsbn("978-1111111111")).andReturn(borrowedBook);

        // Expect repository save calls
        mockBookRepository.save(borrowedBook);
        EasyMock.expectLastCall();
        mockUserRepository.save(userWithBorrowedBooks);
        EasyMock.expectLastCall();

        // Expect email confirmation
        mockEmailService.sendReturnConfirmation("alice@example.com", "Currently Borrowed");
        EasyMock.expectLastCall();

        // Replay all mocks
        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.returnBook("U001", "978-1111111111");

        // Assert
        assertTrue("Return should be successful", result);
        assertTrue("Book should be marked as available", borrowedBook.isAvailable());
        assertEquals("User's borrowed count should decrease", 1, userWithBorrowedBooks.getBorrowedBooksCount());

        // Verify all mock interactions
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }

    @Test
    public void testReturnBook_UserNotFound() {
        // Arrange
        EasyMock.expect(mockUserRepository.findById("INVALID_USER")).andReturn(null);
        EasyMock.expect(mockBookRepository.findByIsbn("978-1111111111")).andReturn(borrowedBook);

        // No save or email operations expected
        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.returnBook("INVALID_USER", "978-1111111111");

        // Assert
        assertFalse("Return should fail when user not found", result);

        // Verify mocks - book state should remain unchanged
        assertFalse("Book availability should not change", borrowedBook.isAvailable());
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }

    @Test
    public void testReturnBook_BookNotFound() {
        // Arrange
        EasyMock.expect(mockUserRepository.findById("U001")).andReturn(userWithBorrowedBooks);
        EasyMock.expect(mockBookRepository.findByIsbn("INVALID_ISBN")).andReturn(null);

        // No save or email operations expected
        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.returnBook("U001", "INVALID_ISBN");

        // Assert
        assertFalse("Return should fail when book not found", result);
        assertEquals("User's borrowed count should remain unchanged", 2, userWithBorrowedBooks.getBorrowedBooksCount());

        // Verify mocks
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }

    @Test
    public void testReturnBook_BookAlreadyAvailable() {
        // Arrange - trying to return a book that's already available (not borrowed)
        EasyMock.expect(mockUserRepository.findById("U001")).andReturn(userWithBorrowedBooks);
        EasyMock.expect(mockBookRepository.findByIsbn("978-2222222222")).andReturn(alreadyAvailableBook);

        // No save or email operations expected since a book is already available
        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.returnBook("U001", "978-2222222222");

        // Assert
        assertFalse("Return should fail when book is already available", result);
        assertTrue("Book should remain available", alreadyAvailableBook.isAvailable());
        assertEquals("User's borrowed count should remain unchanged", 2, userWithBorrowedBooks.getBorrowedBooksCount());

        // Verify mocks
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }

    @Test
    public void testReturnBook_LastBookReturn() {
        // Arrange - User returning their last borrowed book
        User userWithOneBook = new User("U003", "Charlie Brown", "charlie@example.com");
        userWithOneBook.setBorrowedBooksCount(1);

        EasyMock.expect(mockUserRepository.findById("U003")).andReturn(userWithOneBook);
        EasyMock.expect(mockBookRepository.findByIsbn("978-1111111111")).andReturn(borrowedBook);

        // Expect to save operations
        mockBookRepository.save(borrowedBook);
        EasyMock.expectLastCall();
        mockUserRepository.save(userWithOneBook);
        EasyMock.expectLastCall();

        // Expect email confirmation
        mockEmailService.sendReturnConfirmation("charlie@example.com", "Currently Borrowed");
        EasyMock.expectLastCall();

        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.returnBook("U003", "978-1111111111");

        // Assert
        assertTrue("Return should be successful", result);
        assertTrue("Book should be available", borrowedBook.isAvailable());
        assertEquals("User should have 0 borrowed books", 0, userWithOneBook.getBorrowedBooksCount());
        assertTrue("User should be able to borrow more books", userWithOneBook.canBorrowMore());

        // Verify mocks
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }
}