package com.letterbook.user;

import com.letterbook.common.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/register")
    public ResponseEntity<UserDtos.AuthResponse> register(@Valid @RequestBody UserDtos.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDtos.LoginRequest req) {
        try {
            return ResponseEntity.ok(service.login(req));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new com.letterbook.common.ApiError(401, "Credenciais inválidas"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody UserDtos.ForgotPasswordRequest req) {
        try {
            service.resetPassword(req);
            return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new com.letterbook.common.ApiError(404, "E-mail não cadastrado"));
        }
    }
}
