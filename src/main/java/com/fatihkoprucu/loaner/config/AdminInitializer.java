package com.fatihkoprucu.loaner.config;

import com.fatihkoprucu.loaner.entity.User;
import com.fatihkoprucu.loaner.enums.RoleType;
import com.fatihkoprucu.loaner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Check if admin user already exists
        if (!userRepository.findByUsername("admin").isPresent()) {
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@loaner.com")
                    .fullName("System Administrator")
                    .role(RoleType.ROLE_ADMIN)
                    .active(true)
                    .build();

            userRepository.save(adminUser);
        }
    }
} 