package com.letterbook.user;

import com.letterbook.common.ConflictException;
import com.letterbook.common.EmailValidator;
import com.letterbook.common.NotFoundException;
import com.letterbook.common.PasswordPolicy;
import com.letterbook.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    public UserDtos.AuthResponse register(UserDtos.RegisterRequest req) {
        if (!EmailValidator.isValid(req.email()))
            throw new IllegalArgumentException("E-mail inválido");
        if (!PasswordPolicy.isStrong(req.password()))
            throw new IllegalArgumentException("Senha deve ter ao menos 8 caracteres, com letra e número");
        if (users.existsByEmail(req.email().toLowerCase()))
            throw new ConflictException("E-mail já cadastrado");

        User u = User.builder()
            .nome(req.nome().trim())
            .email(req.email().trim().toLowerCase())
            .passwordHash(encoder.encode(req.password()))
            .endereco(req.endereco())
            .build();
        u = users.save(u);
        String token = jwt.generate(u.getId(), u.getEmail());
        return new UserDtos.AuthResponse(token, UserDtos.UserView.of(u));
    }

    public UserDtos.AuthResponse login(UserDtos.LoginRequest req) {
        User u = users.findByEmail(req.email().toLowerCase())
            .orElseThrow(() -> new NotFoundException("Credenciais inválidas"));
        if (!encoder.matches(req.password(), u.getPasswordHash()))
            throw new NotFoundException("Credenciais inválidas");
        String token = jwt.generate(u.getId(), u.getEmail());
        return new UserDtos.AuthResponse(token, UserDtos.UserView.of(u));
    }

    public void resetPassword(UserDtos.ForgotPasswordRequest req) {
        if (!PasswordPolicy.isStrong(req.newPassword()))
            throw new IllegalArgumentException("Senha deve ter ao menos 8 caracteres, com letra e número");
        User u = users.findByEmail(req.email().trim().toLowerCase())
            .orElseThrow(() -> new NotFoundException("E-mail não cadastrado"));
        u.setPasswordHash(encoder.encode(req.newPassword()));
        users.save(u);
    }
}
