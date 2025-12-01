package com.crm.app.web.auth;

public record LoginResponse(String token, Boolean enabled) {
}