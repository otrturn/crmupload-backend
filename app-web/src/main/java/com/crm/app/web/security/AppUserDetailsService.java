package com.crm.app.web.security;

import com.crm.app.dto.UserAccount;
import com.crm.app.port.user.UserAccountRepositoryPort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserAccountRepositoryPort repository;

    public AppUserDetailsService(UserAccountRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        var authorities = user.roles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new org.springframework.security.core.userdetails.User(
                user.username(),
                user.passwordHash(),
                authorities
        );
    }
}