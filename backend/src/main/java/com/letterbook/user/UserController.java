package com.letterbook.user;

import com.letterbook.common.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) { this.users = users; }

    @GetMapping("/me")
    public UserDtos.UserView me(@AuthenticationPrincipal String userId) {
        return UserDtos.UserView.of(users.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado")));
    }

    @PutMapping("/me")
    public UserDtos.UserView updateMe(@AuthenticationPrincipal String userId,
                                      @Valid @RequestBody UserDtos.UpdateRequest req) {
        User u = users.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        u.setNome(req.nome().trim());
        u.setEndereco(req.endereco());
        return UserDtos.UserView.of(users.save(u));
    }
}
