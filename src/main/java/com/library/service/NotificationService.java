package com.library.service;
import com.library.model.User;
import com.library.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

public class NotificationService {
    private UserRepository userRepository;
    private EmailService emailService;

    public NotificationService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void sendOverdueNotifications() {
        List<User> usersWithOverdueBooks = userRepository.findUsersWithOverdueBooks();

        for (User user : usersWithOverdueBooks) {
            List<String> overdueBookTitles = getOverdueBookTitles(user);
            if (!overdueBookTitles.isEmpty()) {
                emailService.sendOverdueNotification(user.getEmail(), user.getName(), overdueBookTitles);
            }
        }
    }

    private List<String> getOverdueBookTitles(User user) {
        // Simplified - in real app would check dates
        return Arrays.asList("Sample Overdue Book 1", "Sample Overdue Book 2");
    }
}
