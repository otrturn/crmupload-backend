package com.crm.app.web;

import com.crm.app.web.register.ActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ActivationController {

    private final ActivationService activationService;

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam("token") String token) {
        boolean success = activationService.activateByToken(token);

        if (success) {
            return ResponseEntity.ok("Ihr Konto wurde erfolgreich freigeschaltet. Sie können sich jetzt einloggen.");
        }

        return ResponseEntity.badRequest()
                .body("Der Aktivierungslink ist ungültig oder abgelaufen.");
    }
}
