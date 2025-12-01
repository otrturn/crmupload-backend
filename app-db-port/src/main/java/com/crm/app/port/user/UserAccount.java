package com.crm.app.port.user;

import java.util.List;

public record UserAccount(
        Long id,
        String username,
        String passwordHash,
        List<String> roles
) {
}
