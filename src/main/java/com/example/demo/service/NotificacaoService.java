package com.example.demo.service;

import com.example.demo.messaging.AlertaMensagem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class NotificacaoService {

    private final RestClient restClient;
    private final String chatId;

    public NotificacaoService(
            @Value("${telegram.bot-token}") String token,
            @Value("${telegram.chat-id}") String chatId) {
        this.restClient = RestClient.create("https://api.telegram.org/bot" + token);
        this.chatId = chatId;
    }

    public void enviar(AlertaMensagem mensagem) {
        String texto = "🔔 %s\nPreço atual: R$ %.2f\n%s".formatted(
                mensagem.ticker(), mensagem.preco(), mensagem.descricao());

        restClient.post()
                .uri("/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "chat_id", chatId,
                        "text", texto))
                .retrieve()
                .toBodilessEntity();
    }
}
