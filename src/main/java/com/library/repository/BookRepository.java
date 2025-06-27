package com.library.repository;

import com.library.model.Book;
import java.util.List;

public interface BookRepository {
    Book findByIsbn(String isbn);
    void save(Book book);
    List<Book> findAvailableBooks();
}