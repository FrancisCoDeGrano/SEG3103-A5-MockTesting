package com.library.service;

import com.library.model.Book;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;

import java.util.List;

public class LibraryService {
    private BookRepository bookRepository;
    private UserRepository userRepository;
    private EmailService emailService;

    public LibraryService(BookRepository bookRepository,
                          UserRepository userRepository,
                          EmailService emailService) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public boolean borrowBook(String userId, String isbn) {
        User user = userRepository.findById(userId);
        if (user == null || !user.canBorrowMore()) {
            return false;
        }

        Book book = bookRepository.findByIsbn(isbn);
        if (book == null || !book.isAvailable()) {
            return false;
        }

        book.setAvailable(false);
        user.setBorrowedBooksCount(user.getBorrowedBooksCount() + 1);

        bookRepository.save(book);
        userRepository.save(user);
        emailService.sendBorrowConfirmation(user.getEmail(), book.getTitle());

        return true;
    }

    public boolean returnBook(String userId, String isbn) {
        User user = userRepository.findById(userId);
        Book book = bookRepository.findByIsbn(isbn);

        if (user == null || book == null || book.isAvailable()) {
            return false;
        }

        book.setAvailable(true);
        user.setBorrowedBooksCount(user.getBorrowedBooksCount() - 1);

        bookRepository.save(book);
        userRepository.save(user);
        emailService.sendReturnConfirmation(user.getEmail(), book.getTitle());

        return true;
    }

    public List<Book> searchAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
}
