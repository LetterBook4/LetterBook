package com.letterbook.cep;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.letterbook.common.NotFoundException;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class CepServiceVcrTest {

    static WireMockServer wm;
    static CepService service;

    @BeforeAll
    static void up() throws Exception {
        wm = new WireMockServer(0);
        wm.start();
        String okBody = new String(Files.readAllBytes(
            Paths.get("src/test/resources/vcr/viacep-01310100.json")));
        String errBody = new String(Files.readAllBytes(
            Paths.get("src/test/resources/vcr/viacep-99999999.json")));
        wm.stubFor(get(urlEqualTo("/01310100/json/"))
            .willReturn(aResponse().withHeader("Content-Type","application/json").withBody(okBody)));
        wm.stubFor(get(urlEqualTo("/99999999/json/"))
            .willReturn(aResponse().withHeader("Content-Type","application/json").withBody(errBody)));
        service = new CepService("http://localhost:" + wm.port());
    }

    @AfterAll static void down() { wm.stop(); }

    @Test
    void buscaCepValidoRetornaEndereco() {
        CepResponse r = service.lookup("01310-100");
        assertEquals("Bela Vista", r.bairro());
        assertEquals("São Paulo", r.localidade());
        assertEquals("SP", r.uf());
    }

    @Test
    void cepInexistenteLanca404() {
        assertThrows(NotFoundException.class, () -> service.lookup("99999999"));
    }

    @Test
    void cepFormatoInvalidoLancaBadRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.lookup("123"));
    }
}
