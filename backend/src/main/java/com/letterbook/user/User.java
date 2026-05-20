package com.letterbook.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String nome;
    @Indexed(unique = true)
    private String email;
    private String passwordHash;
    private Address endereco;
}
