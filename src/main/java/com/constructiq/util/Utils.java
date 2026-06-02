package com.constructiq.util;

import com.constructiq.entity.User;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 12:50 am
 */

@RequiredArgsConstructor
@Component
public class Utils {

    private final UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
}
