package com.example.demo.controller;

import com.example.demo.dto.AlertaRequestDTO;
import com.example.demo.messaging.AlertaMensagem;
import com.example.demo.messaging.AlertaProducer;
import com.example.demo.model.Alerta;
import com.example.demo.service.AlertaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    private final AlertaProducer producer;

    public AlertaController(AlertaProducer producer, AlertaService alertaService) {
        this.producer = producer;
        this.alertaService = alertaService;
    }

    @PostMapping
    public Alerta criar(@RequestBody AlertaRequestDTO request) {
        return alertaService.criar(request);
    }

    @GetMapping
    public List<Alerta> listar() {
        return alertaService.listar();
    }

    @PostMapping("/teste")
    public String teste(@RequestBody AlertaMensagem mensagem) {
        producer.publicar(mensagem);
        return "Mensagem publicada";
    }
}
