package com.library.service;

import com.library.model.User;
import com.library.repository.UserRepository;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Elaborate test case for ZAK
 * Testing NotificationService.sendOverdueNotifications() method
 */
public class NotificationServiceTest {

    private NotificationService notificationService;
    private UserRepository mockUserRepository;
    private EmailService mockEmailService;

    // Test data
    private User userWithOverdueBooks1;
    private User userWithOverdueBooks2;
    private User userWithNoOverdueBooks;
    private List<String> overdueBookTitles;

    @Before
    public void setUp() {
        // Create mocks
        mockUserRepository = EasyMock.createMock(UserRepository.class);
        mockEmailService = EasyMock.createMock(EmailService.class);

        // Create a service with injected mocks
        notificationService = new NotificationService(mockUserRepository, mockEmailService);

        // Set up test data
        userWithOverdueBooks1 = new User("U001", "David Miller", "david@example.com");
        userWithOverdueBooks2 = new User("U002", "Emma Davis", "emma@example.com");
        userWithNoOverdueBooks = new User("U003", "Frank Wilson", "frank@example.com");

        overdueBookTitles = Arrays.asList("Sample Overdue Book 1", "Sample Overdue Book 2");
    }

    @Test
    public void testSendOverdueNotifications_MultipleUsersWithOverdueBooks() {
        // Arrange - Multiple users with overdue books
        List<User> usersWithOverdue = Arrays.asList(userWithOverdueBooks1, userWithOverdueBooks2);

        EasyMock.expect(mockUserRepository.findUsersWithOverdueBooks()).andReturn(usersWithOverdue);

        // Expect email notifications for both users
        mockEmailService.sendOverdueNotification(
                "david@example.com",
                "David Miller",
                overdueBookTitles
        );
        EasyMock.expectLastCall();

        mockEmailService.sendOverdueNotification(
                "emma@example.com",
                "Emma Davis",
                overdueBookTitles
        );
        EasyMock.expectLastCall();

        EasyMock.replay(mockUserRepository, mockEmailService);

        // Act
        notificationService.sendOverdueNotifications();

        // Assert & Verify - All expected interactions occurred
        EasyMock.verify(mockUserRepository, mockEmailService);
    }

    @Test
    public void testSendOverdueNotifications_SingleUserWithOverdueBooks() {
        // Arrange - Only one user with overdue books
        List<User> usersWithOverdue = Arrays.asList(userWithOverdueBooks1);

        EasyMock.expect(mockUserRepository.findUsersWithOverdueBooks()).andReturn(usersWithOverdue);

        // Expect email notification for a single user
        mockEmailService.sendOverdueNotification(
                "david@example.com",
                "David Miller",
                overdueBookTitles
        );
        EasyMock.expectLastCall();

        EasyMock.replay(mockUserRepository, mockEmailService);

        // Act
        notificationService.sendOverdueNotifications();

        // Assert & Verify
        EasyMock.verify(mockUserRepository, mockEmailService);
    }

    @Test
    public void testSendOverdueNotifications_NoUsersWithOverdueBooks() {
        // Arrange - No users with overdue books
        List<User> emptyUserList = Collections.emptyList();

        EasyMock.expect(mockUserRepository.findUsersWithOverdueBooks()).andReturn(emptyUserList);

        // No email service calls expected
        EasyMock.replay(mockUserRepository, mockEmailService);

        // Act
        notificationService.sendOverdueNotifications();

        // Assert & Verify - No email notifications should be sent
        EasyMock.verify(mockUserRepository, mockEmailService);
    }

    @Test
    public void testSendOverdueNotifications_LargeUserList() {
        // Arrange - Test with a larger list to verify batch processing
        User user3 = new User("U003", "Grace Taylor", "grace@example.com");
        User user4 = new User("U004", "Henry Johnson", "henry@example.com");
        User user5 = new User("U005", "Iris Brown", "iris@example.com");

        List<User> largeUserList = Arrays.asList(
                userWithOverdueBooks1,
                userWithOverdueBooks2,
                user3,
                user4,
                user5
        );

        EasyMock.expect(mockUserRepository.findUsersWithOverdueBooks()).andReturn(largeUserList);

        // Expect email notifications for all users
        mockEmailService.sendOverdueNotification("david@example.com", "David Miller", overdueBookTitles);
        EasyMock.expectLastCall();

        mockEmailService.sendOverdueNotification("emma@example.com", "Emma Davis", overdueBookTitles);
        EasyMock.expectLastCall();

        mockEmailService.sendOverdueNotification("grace@example.com", "Grace Taylor", overdueBookTitles);
        EasyMock.expectLastCall();

        mockEmailService.sendOverdueNotification("henry@example.com", "Henry Johnson", overdueBookTitles);
        EasyMock.expectLastCall();

        mockEmailService.sendOverdueNotification("iris@example.com", "Iris Brown", overdueBookTitles);
        EasyMock.expectLastCall();

        EasyMock.replay(mockUserRepository, mockEmailService);

        // Act
        notificationService.sendOverdueNotifications();

        // Assert & Verify - All 5 email notifications should be sent
        EasyMock.verify(mockUserRepository, mockEmailService);
    }

    @Test
    public void testSendOverdueNotifications_RepositoryException() {
        // Arrange - Test error handling when repository throws exception
        EasyMock.expect(mockUserRepository.findUsersWithOverdueBooks())
                .andThrow(new RuntimeException("Database connection failed"));

        // No email service calls expected due to the exception
        EasyMock.replay(mockUserRepository, mockEmailService);

        // Act & Assert - Exception should be thrown
        try {
            notificationService.sendOverdueNotifications();
            // If we reach here, the test should fail
            assert false : "Expected RuntimeException was not thrown";
        } catch (RuntimeException e) {
            // Expected behavior
            assert "Database connection failed".equals(e.getMessage());
        }

        // Verify mocks
        EasyMock.verify(mockUserRepository, mockEmailService);
    }

    @Test
    public void testSendOverdueNotifications_EmailServiceException() {
        // Arrange - Test handling when email service fails
        List<User> usersWithOverdue = Arrays.asList(userWithOverdueBooks1, userWithOverdueBooks2);

        EasyMock.expect(mockUserRepository.findUsersWithOverdueBooks()).andReturn(usersWithOverdue);

        // First email succeeds, second email fails
        mockEmailService.sendOverdueNotification("david@example.com", "David Miller", overdueBookTitles);
        EasyMock.expectLastCall();

        mockEmailService.sendOverdueNotification("emma@example.com", "Emma Davis", overdueBookTitles);
        EasyMock.expectLastCall().andThrow(new RuntimeException("Email service unavailable"));

        EasyMock.replay(mockUserRepository, mockEmailService);

        // Act & Assert - Exception should be thrown after the first-email is sent
        try {
            notificationService.sendOverdueNotifications();
            assert false : "Expected RuntimeException was not thrown";
        } catch (RuntimeException e) {
            assert "Email service unavailable".equals(e.getMessage());
        }

        // Verify mocks
        EasyMock.verify(mockUserRepository, mockEmailService);
    }
}