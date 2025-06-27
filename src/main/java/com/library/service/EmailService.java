package com.library.service;

import java.util.List;

public interface EmailService {
    void sendBorrowConfirmation(String email, String bookTitle);
    void sendOverdueNotification(String email, String userName, List<String> overdueBooks);
    void sendReturnConfirmation(String email, String bookTitle);
}