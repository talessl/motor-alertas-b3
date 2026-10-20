package com.example.demo.service;

import com.example.demo.dto.AlertaRequestDTO;
import com.example.demo.model.Alerta;
import com.example.demo.model.Ativo;
import com.example.demo.repository.AlertaRepository;
import com.example.demo.repository.AtivoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final AtivoRepository ativoRepository;

    public AlertaService(AlertaRepository alertaRepository, AtivoRepository ativoRepository) {
        this.alertaRepository = alertaRepository;
        this.ativoRepository = ativoRepository;
    }

    public Alerta criar(AlertaRequestDTO request) {
        Ativo ativo = ativoRepository.findByTicker(request.ticker())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ativo não cadastrado: " + request.ticker()));

        Alerta alerta = new Alerta();
        alerta.setAtivo(ativo);
        alerta.setCondicao(request.condicao());
        alerta.setValorAlvo(request.valorAlvo());
        alerta.setLimiteInferior(request.limiteInferior());
        alerta.setLimiteSuperior(request.limiteSuperior());
        if (request.habilitado() != null) {
            alerta.setHabilitado(request.habilitado());
        }
        return alertaRepository.save(alerta);
    }

    public List<Alerta> listar() {
        return alertaRepository.findAll();
    }
}
