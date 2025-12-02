package com.crm.app;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@SuppressWarnings("squid:S6437")
public class AppGenerateToken {
    public static void main(String[] args) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String raw = "test123"; // dein Klartext-Passwort
        String hash = encoder.encode(raw);
        System.out.println(hash);

        System.out.println("matches: " + encoder.matches(raw, hash));
        System.out.println("matches: " + encoder.matches(raw, "{bcrypt}$2a$10$2KpcUEUrGLOwlfWVa4ojwu7DsRE/iBZHfsqucPWARrACuS.o52ihq"));
    }
}
