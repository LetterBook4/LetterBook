package com.letterbook.book;

import jakarta.validation.constraints.*;

public class BookDtos {
    public record CreateOrUpdate(
        @NotBlank String titulo,
        @NotBlank String autor,
        @Min(1000) @Max(2099) Integer ano,
        @Min(0) @Max(5) Integer nota,
        String genero,
        String editora,
        @Min(1) @Max(100000) Integer paginas,
        String sinopse,
        String avaliacao,
        String capaUrl,
        boolean lendo,
        String inicio,
        String termino) {}

    public record View(String id, String titulo, String autor, Integer ano, Integer nota,
                       String genero, String editora, Integer paginas, String sinopse,
                       String avaliacao, String capaUrl, boolean lendo,
                       String inicio, String termino) {
        public static View of(Book b) {
            return new View(b.getId(), b.getTitulo(), b.getAutor(), b.getAno(), b.getNota(),
                b.getGenero(), b.getEditora(), b.getPaginas(), b.getSinopse(),
                b.getAvaliacao(), b.getCapaUrl(), b.isLendo(),
                b.getInicio(), b.getTermino());
        }
    }
}
