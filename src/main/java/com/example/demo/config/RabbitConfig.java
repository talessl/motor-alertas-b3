package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String FILA_ALERTAS = "alertas.fila";
    public static final String EXCHANGE = "alertas.exchange";
    public static final String ROUTING_KEY = "alertas.disparado";
    public static final String FILA_PRECOS = "precos.fila";
    public static final String ROUTING_KEY_PRECOS = "preco.atualizado";

    @Bean
    public Queue filaPrecos() {
        return new Queue(FILA_PRECOS, true);
    }

    @Bean
    public Binding bindingPrecos(Queue filaPrecos, DirectExchange exchange) {
        return BindingBuilder.bind(filaPrecos).to(exchange).with(ROUTING_KEY_PRECOS);
    }

    @Bean
    public Queue filaAlertas() {
        return new Queue(FILA_ALERTAS, true); // true = durável (sobrevive a reinício)
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue filaAlertas, DirectExchange exchange) {
        return BindingBuilder.bind(filaAlertas).to(exchange).with(ROUTING_KEY);
    }

    // Converte objetos para JSON automaticamente
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
