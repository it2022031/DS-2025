package com.example.demo.Services;

import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }

    public Long getNextId() {
        return userRepository.count() + 1;
    }

    public User addUserAnalytical(User user) {
        return userRepository.save(user);
    }
}
