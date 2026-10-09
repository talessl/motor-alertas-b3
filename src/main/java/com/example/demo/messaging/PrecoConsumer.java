package com.example.demo.messaging;

import com.example.demo.config.RabbitConfig;
import com.example.demo.service.AvaliadorAlertaService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PrecoConsumer {

    private final AvaliadorAlertaService avaliador;

    public PrecoConsumer(AvaliadorAlertaService avaliador) {
        this.avaliador = avaliador;
    }

    @RabbitListener(queues = RabbitConfig.FILA_PRECOS)
    public void receber(PrecoMensagem mensagem) {
        avaliador.avaliar(mensagem);
    }
}