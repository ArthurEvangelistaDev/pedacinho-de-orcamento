package br.com.pedacinhodeorcamento.insumos;

import java.net.URI;
import java.util.*;
import br.com.pedacinhodeorcamento.api.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/insumos")
class InsumoController {
    private final InsumoService service;
    InsumoController(InsumoService service) { this.service=service; }
    @PostMapping(consumes="application/json",produces="application/json")
    ResponseEntity<Map<String,Object>> criar(@RequestBody Map<String,Object> body) {
        var result=service.criar(body);
        return ResponseEntity.created(URI.create("/api/v1/insumos/"+result.get("id"))).body(result);
    }
    @GetMapping(value="/{id}",produces="application/json")
    Map<String,Object> buscar(@PathVariable String id) {
        long parsed;
        try {
            if(id.length()>19 || !id.matches("[1-9][0-9]*")) throw new NumberFormatException();
            parsed=Long.parseLong(id);
        } catch(NumberFormatException invalid) {
            throw ApiException.validacao(List.of(new ApiException.ErroCampo("id","ID_INVALIDO","Informe um ID inteiro positivo válido.")));
        }
        return service.buscar(parsed);
    }
}
