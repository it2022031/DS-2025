package com.example.demo.Services;

import com.example.demo.Entities.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>(
            List.of(
                    new User(1L, "Alice"),
                    new User(2L, "Bob"),
                    new User(3L, "Charlie")
            )
    );

    public List<User> findAll() {
        return users;
    }

    public User addUser(User user) {
        users.add(user);
        return user;
    }

    public Long getNextId() {
        return (long) (users.size() + 1);
    }
}


