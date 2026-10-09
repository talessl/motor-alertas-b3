package com.example.demo.controller;

import com.example.demo.model.Ativo;
import com.example.demo.service.AtivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Diz ao Spring que esta classe responde requisições REST (retorna JSON)
@RequestMapping("/api/ativos") // A URL base para este controller
public class AtivoController {



    @Autowired
    private AtivoService ativoService;

    // POST: /api/ativos
    @PostMapping
    public ResponseEntity<Ativo> criarAtivo(@RequestBody Ativo ativo) {
        Ativo ativoSalvo = ativoService.salvar(ativo);
        return new ResponseEntity<>(ativoSalvo, HttpStatus.CREATED);
    }

    // GET: /api/ativos
    @GetMapping
    public ResponseEntity<List<Ativo>> listarAtivos() {
        return ResponseEntity.ok(ativoService.listarTodos());
    }
}
