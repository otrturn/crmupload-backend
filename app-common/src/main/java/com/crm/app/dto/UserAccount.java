package com.crm.app.dto;

import java.util.List;

public record UserAccount(
        Long id,
        String username,
        String passwordHash,
        List<String> roles
) {
}
