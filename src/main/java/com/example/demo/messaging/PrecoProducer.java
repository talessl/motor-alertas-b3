package com.example.demo.messaging;

import com.example.demo.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PrecoProducer {

    private final RabbitTemplate rabbitTemplate;

    public PrecoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(PrecoMensagem mensagem) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY_PRECOS,
                mensagem
        );
    }
}