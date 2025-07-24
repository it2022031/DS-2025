package com.example.demo.Services;

import com.example.demo.Entities.User;
import com.example.demo.Entities.UserRole;
import com.example.demo.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public User addRoleToUser(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.addRole(role);
        return userRepository.save(user);
    }
}
