package com.crm.app.port.user;

import java.util.Optional;

public interface UserAccountRepositoryPort {

    Optional<UserAccount> findByUsername(String username);
}