package com.letterbook.book;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService service;
    public BookController(BookService service) { this.service = service; }

    @GetMapping
    public List<BookDtos.View> list(@AuthenticationPrincipal String userId,
                                    @RequestParam(required = false) String search) {
        return service.listMine(userId, search).stream().map(BookDtos.View::of).toList();
    }

    @GetMapping("/{id}")
    public BookDtos.View get(@AuthenticationPrincipal String userId, @PathVariable String id) {
        return BookDtos.View.of(service.getOwned(userId, id));
    }

    @PostMapping
    public ResponseEntity<BookDtos.View> create(@AuthenticationPrincipal String userId,
                                                @Valid @RequestBody BookDtos.CreateOrUpdate dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BookDtos.View.of(service.create(userId, dto)));
    }

    @PutMapping("/{id}")
    public BookDtos.View update(@AuthenticationPrincipal String userId,
                                @PathVariable String id,
                                @Valid @RequestBody BookDtos.CreateOrUpdate dto) {
        return BookDtos.View.of(service.update(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal String userId, @PathVariable String id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
