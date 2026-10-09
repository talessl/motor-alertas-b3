package com.example.demo.service;

import com.example.demo.messaging.AlertaMensagem;
import com.example.demo.messaging.AlertaProducer;
import com.example.demo.messaging.PrecoMensagem;
import com.example.demo.model.Alerta;
import com.example.demo.model.Condicao;
import com.example.demo.repository.AlertaRepository;
import com.example.demo.repository.AtivoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvaliadorAlertaService {

    private static final Duration INTERVALO_MINIMO = Duration.ofMinutes(1);

    private final AlertaRepository alertaRepository;
    private final AtivoRepository ativoRepository;
    private final AlertaProducer alertaProducer;

    public AvaliadorAlertaService(AlertaRepository alertaRepository,
                                  AtivoRepository ativoRepository,
                                  AlertaProducer alertaProducer) {
        this.alertaRepository = alertaRepository;
        this.ativoRepository = ativoRepository;
        this.alertaProducer = alertaProducer;
    }

    @Transactional
    public void avaliar(PrecoMensagem msg) {
        // atualiza o último preço do ativo
        ativoRepository.findByTicker(msg.ticker()).ifPresent(a -> {
            a.setUltimoPreco(msg.preco().doubleValue());
            ativoRepository.save(a);
        });

        List<Alerta> alertas = alertaRepository
                .findByAtivoTickerAndHabilitadoTrue(msg.ticker());

        for (Alerta alerta : alertas) {
            if (condicaoAtendida(alerta, msg.preco()) && podeDisparar(alerta)) {
                alerta.setUltimoDisparo(LocalDateTime.now());
                alertaRepository.save(alerta);

                String sentido = alerta.getCondicao() == Condicao.ACIMA_DE ? "acima de" : "abaixo de";
                String descricao = "%s está %s R$ %.2f".formatted(
                        msg.ticker(), sentido, alerta.getValorAlvo());
                alertaProducer.publicar(
                        new AlertaMensagem(msg.ticker(), msg.preco().doubleValue(), descricao));
            }
        }
    }

    private boolean condicaoAtendida(Alerta alerta, BigDecimal preco) {
        return switch (alerta.getCondicao()) {
            case ACIMA_DE -> preco.compareTo(alerta.getValorAlvo()) > 0;
            case ABAIXO_DE -> preco.compareTo(alerta.getValorAlvo()) < 0;
        };
    }

    private boolean podeDisparar(Alerta alerta) {
        return alerta.getUltimoDisparo() == null
                || Duration.between(alerta.getUltimoDisparo(), LocalDateTime.now())
                .compareTo(INTERVALO_MINIMO) >= 0;
    }
}