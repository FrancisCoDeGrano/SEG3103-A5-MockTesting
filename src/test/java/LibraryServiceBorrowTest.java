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
 * Sample elaborate test case for FRANCISCO
 * Testing LibraryService.borrowBook() method
 */
public class LibraryServiceBorrowTest {

    private LibraryService libraryService;
    private BookRepository mockBookRepository;
    private UserRepository mockUserRepository;
    private EmailService mockEmailService;

    // Test data
    private User validUser;
    private User userAtLimit;
    private Book availableBook;
    private Book unavailableBook;

    @Before
    public void setUp() {
        // Create mocks
        mockBookRepository = EasyMock.createMock(BookRepository.class);
        mockUserRepository = EasyMock.createMock(UserRepository.class);
        mockEmailService = EasyMock.createMock(EmailService.class);

        // Create a service with injected mocks
        libraryService = new LibraryService(mockBookRepository, mockUserRepository, mockEmailService);

        // Set up test data
        validUser = new User("U001", "John Doe", "john@example.com");
        validUser.setBorrowedBooksCount(1); // Can borrow 2 more

        userAtLimit = new User("U002", "Jane Smith", "jane@example.com");
        userAtLimit.setBorrowedBooksCount(3); // At max limit

        availableBook = new Book("978-1234567890", "Test Book", "Test Author");
        unavailableBook = new Book("978-0987654321", "Borrowed Book", "Another Author");
        unavailableBook.setAvailable(false);
    }

    @Test
    public void testBorrowBook_SuccessfulBorrow() {
        // Arrange - Set up mock expectations
        EasyMock.expect(mockUserRepository.findById("U001")).andReturn(validUser);
        EasyMock.expect(mockBookRepository.findByIsbn("978-1234567890")).andReturn(availableBook);

        // Expect repository save calls
        mockBookRepository.save(availableBook);
        EasyMock.expectLastCall();
        mockUserRepository.save(validUser);
        EasyMock.expectLastCall();

        // Expect email service call
        mockEmailService.sendBorrowConfirmation("john@example.com", "Test Book");
        EasyMock.expectLastCall();

        // Replay all mocks
        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.borrowBook("U001", "978-1234567890");

        // Assert
        assertTrue("Borrow should be successful", result);
        assertFalse("Book should be marked as unavailable", availableBook.isAvailable());
        assertEquals("User's borrowed count should increase", 2, validUser.getBorrowedBooksCount());

        // Verify all mock interactions
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }

    @Test
    public void testBorrowBook_UserNotFound() {
        // Arrange
        EasyMock.expect(mockUserRepository.findById("INVALID")).andReturn(null);

        // No other interactions expected since method should return early
        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.borrowBook("INVALID", "978-1234567890");

        // Assert
        assertFalse("Borrow should fail when user not found", result);

        // Verify mocks
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }

    @Test
    public void testBorrowBook_UserAtBorrowLimit() {
        // Arrange
        EasyMock.expect(mockUserRepository.findById("U002")).andReturn(userAtLimit);

        // No further interactions expected
        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.borrowBook("U002", "978-1234567890");

        // Assert
        assertFalse("Borrow should fail when user at limit", result);

        // Verify mocks
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }

    @Test
    public void testBorrowBook_BookNotAvailable() {
        // Arrange
        EasyMock.expect(mockUserRepository.findById("U001")).andReturn(validUser);
        EasyMock.expect(mockBookRepository.findByIsbn("978-0987654321")).andReturn(unavailableBook);

        // No save or email operations expected
        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.borrowBook("U001", "978-0987654321");

        // Assert
        assertFalse("Borrow should fail when book unavailable", result);

        // Verify mocks
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }

    @Test
    public void testBorrowBook_BookNotFound() {
        // Arrange
        EasyMock.expect(mockUserRepository.findById("U001")).andReturn(validUser);
        EasyMock.expect(mockBookRepository.findByIsbn("INVALID_ISBN")).andReturn(null);

        EasyMock.replay(mockBookRepository, mockUserRepository, mockEmailService);

        // Act
        boolean result = libraryService.borrowBook("U001", "INVALID_ISBN");

        // Assert
        assertFalse("Borrow should fail when book not found", result);

        // Verify mocks
        EasyMock.verify(mockBookRepository, mockUserRepository, mockEmailService);
    }
}