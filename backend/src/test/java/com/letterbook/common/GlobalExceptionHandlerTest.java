package com.letterbook.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void badRequestShouldReturn400() {
        ResponseEntity<ApiError> response =
                handler.badRequest(new IllegalArgumentException("Erro teste"));

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Erro teste", response.getBody().message());
    }

    @Test
    void notFoundShouldReturn404() {
        ResponseEntity<ApiError> response =
                handler.notFound(new NotFoundException("Não encontrado"));

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Não encontrado", response.getBody().message());
    }

    @Test
    void conflictShouldReturn409() {
        ResponseEntity<ApiError> response =
                handler.conflict(new ConflictException("Conflito"));

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflito", response.getBody().message());
    }

    @Test
    void forbiddenShouldReturn403() {
        ResponseEntity<ApiError> response =
                handler.forbidden(new AccessDeniedException("Sem acesso"));

        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("Acesso negado", response.getBody().message());
    }
}   