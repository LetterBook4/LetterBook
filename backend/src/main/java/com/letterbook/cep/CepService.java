package com.letterbook.cep;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letterbook.common.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class CepService {

    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;

    public CepService(@Value("${app.cep.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public CepResponse lookup(String cep) {
        String clean = cep == null ? "" : cep.replaceAll("\\D", "");
        if (clean.length() != 8) throw new IllegalArgumentException("CEP deve ter 8 dígitos");
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + clean + "/json/"))
                .timeout(Duration.ofSeconds(5))
                .GET().build();
            HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() >= 500) throw new RuntimeException("ViaCEP indisponível");
            CepResponse body = mapper.readValue(r.body(), CepResponse.class);
            if (body.erro() != null && body.erro()) throw new NotFoundException("CEP não encontrado");
            return body;
        } catch (NotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao consultar ViaCEP: " + e.getMessage(), e);
        }
    }
}
