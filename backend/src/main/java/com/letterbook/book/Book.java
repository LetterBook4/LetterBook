package com.letterbook.book;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "books")
public class Book {
    @Id private String id;
    private String ownerId;
    private String titulo;
    private String autor;
    private Integer ano;
    private Integer nota;       // 0..5
    private String genero;
    private String editora;
    private Integer paginas;
    private String sinopse;
    private String avaliacao;
    private String capaUrl;     // pode ser data URL
    private boolean lendo;
    private String inicio;      // ISO date
    private String termino;
}
