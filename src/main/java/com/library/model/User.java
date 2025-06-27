package com.library.model;

public class User {
    private String userId;
    private String name;
    private String email;
    private int borrowedBooksCount;
    private static final int MAX_BOOKS = 3;

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.borrowedBooksCount = 0;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getBorrowedBooksCount() { return borrowedBooksCount; }
    public void setBorrowedBooksCount(int count) { this.borrowedBooksCount = count; }
    public boolean canBorrowMore() { return borrowedBooksCount < MAX_BOOKS; }
}