package com.example.demo.messaging;

import com.example.demo.config.RabbitConfig;
import com.example.demo.service.NotificacaoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AlertaConsumer {

    private final NotificacaoService notificacaoService;

    public AlertaConsumer(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @RabbitListener(queues = RabbitConfig.FILA_ALERTAS)
    public void receber(AlertaMensagem mensagem) {
        notificacaoService.enviar(mensagem);
    }
}
