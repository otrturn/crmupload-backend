package com.crm.app.web.security;

import com.crm.app.port.user.UserAccount;
import com.crm.app.port.user.UserAccountRepositoryPort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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
                user.passwordHash(),  // genau der Delegating-Hash mit {bcrypt}
                authorities
        );
    }
}