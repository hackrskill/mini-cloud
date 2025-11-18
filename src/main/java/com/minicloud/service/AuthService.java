package com.minicloud.service;

import com.minicloud.exception.UnauthorizedException;
import com.minicloud.model.AppUser;
import com.minicloud.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser register(String name, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with this email already exists");
        }
        AppUser user = new AppUser();
        user.setName(name);
        user.setEmail(email.toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setApiToken(generateToken());
        return userRepository.save(user);
    }

    public AppUser login(String email, String rawPassword) {
        AppUser user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        user.setApiToken(generateToken());
        return userRepository.save(user);
    }

    public AppUser requireUser(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Missing auth token");
        }
        return userRepository.findByApiToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid auth token"));
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}


