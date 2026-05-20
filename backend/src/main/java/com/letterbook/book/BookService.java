package com.letterbook.book;

import com.letterbook.common.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository repo;
    public BookService(BookRepository repo) { this.repo = repo; }

    public List<Book> listMine(String ownerId, String search) {
        if (search != null && !search.isBlank()) {
            return repo.searchByOwner(ownerId, search.trim());
        }
        return repo.findByOwnerId(ownerId);
    }

    public Book getOwned(String ownerId, String id) {
        Book b = repo.findById(id).orElseThrow(() -> new NotFoundException("Livro não encontrado"));
        if (!ownerId.equals(b.getOwnerId()))
            throw new AccessDeniedException("Livro pertence a outro usuário");
        return b;
    }

    public Book create(String ownerId, BookDtos.CreateOrUpdate dto) {
        Book b = Book.builder()
            .ownerId(ownerId)
            .titulo(dto.titulo()).autor(dto.autor()).ano(dto.ano()).nota(dto.nota())
            .genero(dto.genero()).editora(dto.editora()).paginas(dto.paginas())
            .sinopse(dto.sinopse()).avaliacao(dto.avaliacao()).capaUrl(dto.capaUrl())
            .lendo(dto.lendo()).inicio(dto.inicio()).termino(dto.termino())
            .build();
        return repo.save(b);
    }

    public Book update(String ownerId, String id, BookDtos.CreateOrUpdate dto) {
        Book b = getOwned(ownerId, id);
        b.setTitulo(dto.titulo()); b.setAutor(dto.autor()); b.setAno(dto.ano());
        b.setNota(dto.nota()); b.setGenero(dto.genero());
        b.setEditora(dto.editora()); b.setPaginas(dto.paginas());
        b.setSinopse(dto.sinopse()); b.setAvaliacao(dto.avaliacao());
        b.setCapaUrl(dto.capaUrl()); b.setLendo(dto.lendo());
        b.setInicio(dto.inicio()); b.setTermino(dto.termino());
        return repo.save(b);
    }

    public void delete(String ownerId, String id) {
        Book b = getOwned(ownerId, id);
        repo.deleteById(b.getId());
    }
}
