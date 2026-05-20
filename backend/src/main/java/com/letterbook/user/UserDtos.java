package com.letterbook.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public class UserDtos {
    public record RegisterRequest(
        @NotBlank String nome,
        @NotBlank String email,
        @NotBlank String password,
        @Valid Address endereco) {}

    public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password) {}

    public record UpdateRequest(
        @NotBlank String nome,
        @Valid Address endereco) {}

    public record ForgotPasswordRequest(
        @NotBlank String email,
        @NotBlank String newPassword) {}

    public record AuthResponse(String token, UserView user) {}

    public record UserView(String id, String nome, String email, Address endereco) {
        public static UserView of(User u) {
            return new UserView(u.getId(), u.getNome(), u.getEmail(), u.getEndereco());
        }
    }
}
