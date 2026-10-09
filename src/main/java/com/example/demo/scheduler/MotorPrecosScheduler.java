package com.example.demo.scheduler;

import com.example.demo.messaging.PrecoMensagem;
import com.example.demo.messaging.PrecoProducer;
import com.example.demo.model.Ativo;
import com.example.demo.repository.AtivoRepository;
import com.example.demo.service.ConsultaB3Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MotorPrecosScheduler {

    private final AtivoRepository ativoRepository;
    private final ConsultaB3Service consultaB3Service;
    private final PrecoProducer precoProducer;

    public MotorPrecosScheduler(AtivoRepository ativoRepository,
                                ConsultaB3Service consultaB3Service,
                                PrecoProducer precoProducer) {
        this.ativoRepository = ativoRepository;
        this.consultaB3Service = consultaB3Service;
        this.precoProducer = precoProducer;
    }

    @Scheduled(fixedRate = 60000)
    public void atualizarPrecosMercado() {
        List<Ativo> ativos = ativoRepository.findAll();

        if (ativos.isEmpty()) return;

        System.out.println("\n--- Buscando cotações reais na B3 ---");

        for (Ativo ativo : ativos) {
            Double precoReal = consultaB3Service.buscarPrecoReal(ativo.getTicker());

            if (precoReal != null) {
                precoProducer.publicar(
                        new PrecoMensagem(ativo.getTicker(), BigDecimal.valueOf(precoReal)));
                System.out.println(ativo.getTicker() + " | Preço publicado: R$ " + precoReal);
            }
        }
    }
}