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

                // Lógica de descrição isolada para manter o método principal limpo
                String descricao = montarDescricaoNotificacao(alerta, msg);

                alertaProducer.publicar(
                        new AlertaMensagem(msg.ticker(), msg.preco().doubleValue(), descricao));
            }
        }
    }

    // --- MÉTODOS PRIVADOS DE REGRA DE NEGÓCIO ---

    private boolean condicaoAtendida(Alerta alerta, BigDecimal preco) {
        return switch (alerta.getCondicao()) {
            case ACIMA_DE -> preco.compareTo(alerta.getValorAlvo()) > 0;
            case ABAIXO_DE -> preco.compareTo(alerta.getValorAlvo()) < 0;
            case SAIU_DO_CAIXOTE ->
                // Rompeu o teto OU perdeu o piso
                    preco.compareTo(alerta.getLimiteSuperior()) > 0 ||
                            preco.compareTo(alerta.getLimiteInferior()) < 0;
        };
    }

    private String montarDescricaoNotificacao(Alerta alerta, PrecoMensagem msg) {
        if (alerta.getCondicao() == Condicao.SAIU_DO_CAIXOTE) {
            // Descobre para qual lado rompeu para avisar o usuário
            boolean rompeuParaCima = msg.preco().compareTo(alerta.getLimiteSuperior()) > 0;
            String direcao = rompeuParaCima ? "PARA CIMA (Resistência rompida)" : "PARA BAIXO (Suporte perdido)";

            return "%s saiu do caixote %s! Preço atual R$ %.2f (Range era R$ %.2f a R$ %.2f)"
                    .formatted(msg.ticker(), direcao, msg.preco(),
                            alerta.getLimiteInferior(), alerta.getLimiteSuperior());
        }

        // Lógica antiga (mantida intacta)
        String sentido = alerta.getCondicao() == Condicao.ACIMA_DE ? "acima de" : "abaixo de";
        return "%s está %s R$ %.2f".formatted(msg.ticker(), sentido, alerta.getValorAlvo());
    }

    private boolean podeDisparar(Alerta alerta) {
        return alerta.getUltimoDisparo() == null
                || Duration.between(alerta.getUltimoDisparo(), LocalDateTime.now())
                .compareTo(INTERVALO_MINIMO) >= 0;
    }
}