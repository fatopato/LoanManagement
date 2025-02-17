package com.fatihkoprucu.loaner.service;

import com.fatihkoprucu.loaner.config.JWTGenerator;
import com.fatihkoprucu.loaner.dto.AuthResponse;
import com.fatihkoprucu.loaner.dto.LoginRequest;
import com.fatihkoprucu.loaner.dto.RegisterRequest;
import com.fatihkoprucu.loaner.entity.User;
import com.fatihkoprucu.loaner.enums.RoleType;
import com.fatihkoprucu.loaner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        
        Long userId = user.getRole() == RoleType.ROLE_CUSTOMER ? user.getId() : null;
        
        List<String> roles = Collections.singletonList(user.getRole().getValue());
        String token = jwtGenerator.generateToken(user.getUsername(), roles);
        return new AuthResponse(token, user.getUsername(), userId);
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        String[] nameParts = request.getFullName().split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Create new user with ROLE_CUSTOMER by default
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(RoleType.ROLE_CUSTOMER)
                .active(true)
                .name(firstName)
                .surname(lastName)
                .creditLimit(new BigDecimal("50000.00"))
                .usedCreditLimit(BigDecimal.ZERO)
                .build();

        user = userRepository.save(user);
        
        List<String> roles = Collections.singletonList(RoleType.ROLE_CUSTOMER.getValue());
        String token = jwtGenerator.generateToken(user.getUsername(), roles);
        return new AuthResponse(token, user.getUsername(), user.getId());
    }
} 