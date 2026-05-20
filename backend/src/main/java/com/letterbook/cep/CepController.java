package com.letterbook.cep;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cep")
public class CepController {
    private final CepService service;
    public CepController(CepService service) { this.service = service; }

    @GetMapping("/{cep}")
    public CepResponse lookup(@PathVariable String cep) {
        return service.lookup(cep);
    }
}
