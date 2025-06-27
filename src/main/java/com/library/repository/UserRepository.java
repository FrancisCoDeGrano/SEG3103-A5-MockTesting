package com.library.repository;

import com.library.model.User;
import java.util.List;

public interface UserRepository {
    User findById(String userId);
    void save(User user);
    List<User> findUsersWithOverdueBooks();
}