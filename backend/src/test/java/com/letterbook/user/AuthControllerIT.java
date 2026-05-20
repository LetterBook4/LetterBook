package com.letterbook.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letterbook.support.AbstractMongoIT;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerIT extends AbstractMongoIT {

    @Autowired TestRestTemplate http;
    @Autowired UserRepository users;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach void clean() { users.deleteAll(); }

    String registerPayload(String email) {
        return """
        {"nome":"Joao","email":"%s","password":"abcdef12",
         "endereco":{"cep":"01310100","rua":"Av Paulista","bairro":"Bela Vista",
                     "cidade":"Sao Paulo","estado":"SP","numero":"100"}}
        """.formatted(email);
    }

    @Test
    void registerThenLoginReturnsJwt() throws Exception {
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> reg = http.postForEntity("/api/auth/register",
            new HttpEntity<>(registerPayload("u1@test.com"), h), String.class);
        assertEquals(HttpStatus.CREATED, reg.getStatusCode());
        assertNotNull(mapper.readTree(reg.getBody()).get("token").asText());

        ResponseEntity<String> login = http.postForEntity("/api/auth/login",
            new HttpEntity<>("{\"email\":\"u1@test.com\",\"password\":\"abcdef12\"}", h),
            String.class);
        assertEquals(HttpStatus.OK, login.getStatusCode());
        JsonNode body = mapper.readTree(login.getBody());
        assertTrue(body.get("token").asText().length() > 20);
        assertEquals("u1@test.com", body.get("user").get("email").asText());
    }

    @Test
    void duplicateEmailReturns409() {
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        http.postForEntity("/api/auth/register",
            new HttpEntity<>(registerPayload("dup@test.com"), h), String.class);
        ResponseEntity<String> r = http.postForEntity("/api/auth/register",
            new HttpEntity<>(registerPayload("dup@test.com"), h), String.class);
        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
    }

    @Test
    void invalidPasswordReturns400() {
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        String bad = """
            {"nome":"X","email":"weak@test.com","password":"short"}
            """;
        ResponseEntity<String> r = http.postForEntity("/api/auth/register",
            new HttpEntity<>(bad, h), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
    }

    @Test
    void loginWithWrongPasswordReturns401() {
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        http.postForEntity("/api/auth/register",
            new HttpEntity<>(registerPayload("ok@test.com"), h), String.class);
        ResponseEntity<String> r = http.postForEntity("/api/auth/login",
            new HttpEntity<>("{\"email\":\"ok@test.com\",\"password\":\"wrongpwd1\"}", h),
            String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
    }
}
