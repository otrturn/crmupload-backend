package com.crm.app.web.register;

import com.crm.app.dto.UserAccount;
import com.crm.app.port.user.UserAccountRepositoryPort;
import com.crm.app.web.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAccountRegistrationService {

    private final UserAccountRepositoryPort userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public UserAccountRegistrationResult registerUserAccount(String emailAddress, String rawPassword) {
        if (userAccountRepository.existsByUsername(emailAddress)) {
            throw new IllegalStateException("Username already exists: " + emailAddress);
        }

        long userId = userAccountRepository.nextUserId();
        String passwordHash = passwordEncoder.encode(rawPassword);

        UserAccount user = new UserAccount(
                userId,
                emailAddress,
                passwordHash,
                List.of("ROLE_USER")
        );
        userAccountRepository.insertUserAccount(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(emailAddress);
        String token = jwtService.generateToken(userDetails);

        return new UserAccountRegistrationResult(userId, token);
    }

}

