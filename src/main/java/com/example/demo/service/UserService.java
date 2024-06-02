package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public User getUserByMobNum(String mobNum) {
        return userRepository.findByMobNum(mobNum);
    }

    public User getUserByPanNum(String panNum) {
        return userRepository.findByPanNum(panNum);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}
