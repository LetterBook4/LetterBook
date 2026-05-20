package com.letterbook.book;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letterbook.support.AbstractMongoIT;
import com.letterbook.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

class BookControllerIT extends AbstractMongoIT {

    @Autowired TestRestTemplate http;
    @Autowired UserRepository users;
    @Autowired BookRepository books;
    ObjectMapper mapper = new ObjectMapper();

    String token;

    @BeforeEach
    void setup() throws Exception {
        users.deleteAll(); books.deleteAll();
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        String payload = """
        {"nome":"Owner","email":"owner@test.com","password":"abcdef12",
         "endereco":{"cep":"01310100","rua":"Av Paulista","bairro":"Bela Vista",
                     "cidade":"Sao Paulo","estado":"SP","numero":"1"}}
        """;
        ResponseEntity<String> r = http.postForEntity("/api/auth/register",
            new HttpEntity<>(payload, h), String.class);
        token = mapper.readTree(r.getBody()).get("token").asText();
    }

    HttpHeaders auth() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(token);
        return h;
    }

    @Test
    void crudFullCycle() throws Exception {
        String create = """
        {"titulo":"Dom Casmurro","autor":"Machado de Assis","ano":1899,"nota":5,
         "genero":"Romance","sinopse":"...","capaUrl":null,"lendo":false,
         "inicio":"2025-01-01","termino":"2025-02-01"}""";
        ResponseEntity<String> c = http.exchange("/api/books", HttpMethod.POST,
            new HttpEntity<>(create, auth()), String.class);
        assertEquals(HttpStatus.CREATED, c.getStatusCode());
        String id = mapper.readTree(c.getBody()).get("id").asText();

        ResponseEntity<String> list = http.exchange("/api/books", HttpMethod.GET,
            new HttpEntity<>(auth()), String.class);
        assertEquals(HttpStatus.OK, list.getStatusCode());
        assertEquals(1, mapper.readTree(list.getBody()).size());

        String update = create.replace("\"nota\":5", "\"nota\":4");
        ResponseEntity<String> u = http.exchange("/api/books/" + id, HttpMethod.PUT,
            new HttpEntity<>(update, auth()), String.class);
        assertEquals(HttpStatus.OK, u.getStatusCode());
        assertEquals(4, mapper.readTree(u.getBody()).get("nota").asInt());

        ResponseEntity<Void> d = http.exchange("/api/books/" + id, HttpMethod.DELETE,
            new HttpEntity<>(auth()), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, d.getStatusCode());
        assertEquals(0, books.count());
    }

    @Test
    void searchFiltersByTitleOrAuthor() throws Exception {
        for (String t : new String[]{"Dom Casmurro|Machado","O Cortiço|Aluísio","1984|Orwell"}) {
            String[] p = t.split("\\|");
            String body = """
            {"titulo":"%s","autor":"%s","ano":1900,"nota":3,"genero":"X",
             "sinopse":"","capaUrl":null,"lendo":false,"inicio":null,"termino":null}
            """.formatted(p[0], p[1]);
            http.exchange("/api/books", HttpMethod.POST,
                new HttpEntity<>(body, auth()), String.class);
        }
        ResponseEntity<String> r = http.exchange("/api/books?search=machado",
            HttpMethod.GET, new HttpEntity<>(auth()), String.class);
        JsonNode arr = mapper.readTree(r.getBody());
        assertEquals(1, arr.size());
        assertEquals("Dom Casmurro", arr.get(0).get("titulo").asText());
    }

    @Test
    void unauthenticatedRequestReturns401() {
        ResponseEntity<String> r = http.getForEntity("/api/books", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
    }

    @Test
    void cannotAccessOtherUsersBook() throws Exception {
        // cria livro do owner
        String create = """
        {"titulo":"Privado","autor":"X","ano":2000,"nota":1,"genero":null,
         "sinopse":"","capaUrl":null,"lendo":false,"inicio":null,"termino":null}""";
        String id = mapper.readTree(http.exchange("/api/books", HttpMethod.POST,
            new HttpEntity<>(create, auth()), String.class).getBody()).get("id").asText();

        // registra outro usuário
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        String other = """
        {"nome":"Other","email":"other@test.com","password":"abcdef12",
         "endereco":{"cep":"01310100","rua":"A","bairro":"B","cidade":"C","estado":"SP","numero":"1"}}
        """;
        String t2 = mapper.readTree(http.postForEntity("/api/auth/register",
            new HttpEntity<>(other, h), String.class).getBody()).get("token").asText();
        HttpHeaders h2 = new HttpHeaders();
        h2.setContentType(MediaType.APPLICATION_JSON); h2.setBearerAuth(t2);

        ResponseEntity<String> r = http.exchange("/api/books/" + id, HttpMethod.GET,
            new HttpEntity<>(h2), String.class);
        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
    }
}
