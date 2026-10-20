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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        System.out.println("\n--- Buscando cotações reais na B3 (Processamento Paralelo) ---");

        // Cria um gerenciador de Threads.
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            for (Ativo ativo : ativos) {
                executor.submit(() -> buscarEPublicar(ativo));
            }

        }
    }

    private void buscarEPublicar(Ativo ativo) {
        try {
            Thread.sleep((long) (Math.random() * 2000));
            Double precoReal = consultaB3Service.buscarPrecoReal(ativo.getTicker());

            if (precoReal != null) {
                precoProducer.publicar(
                        new PrecoMensagem(ativo.getTicker(), BigDecimal.valueOf(precoReal)));
                System.out.println(Thread.currentThread().getName() + " -> " + ativo.getTicker() + " | Publicado: R$ " + precoReal);
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar " + ativo.getTicker() + ": " + e.getMessage());
        }
    }
}