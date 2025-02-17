package com.fatihkoprucu.loaner.service;

import com.fatihkoprucu.loaner.dto.CustomerResponse;
import com.fatihkoprucu.loaner.entity.User;
import com.fatihkoprucu.loaner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        return new CustomerResponse(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getCreditLimit(),
                user.getUsedCreditLimit(),
                user.getCreditLimit().subtract(user.getUsedCreditLimit()),
                null  // loans will be populated by the controller if needed
        );
    }

    @Transactional
    public void updateUsedCreditLimit(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        user.setUsedCreditLimit(user.getUsedCreditLimit().add(amount));
        userRepository.save(user);
    }
} 