package com.example.demo.service;

import com.example.demo.dto.BrapiResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConsultaB3Service {

    // O RestTemplate é o cliente HTTP padrão e mais famoso do Spring Boot
    private final RestTemplate restTemplate = new RestTemplate();

    // Crie uma conta gratuita em brapi.dev para pegar seu token e coloque aqui
    private final String TOKEN = "gGDDMV2fcwjenikMBxXUyz";

    public Double buscarPrecoReal(String ticker) {
        String url = "https://brapi.dev/api/quote/" + ticker + "?token=" + TOKEN;

        try {
            // Faz o GET e converte o JSON automaticamente para o nosso DTO!
            BrapiResponseDTO response = restTemplate.getForObject(url, BrapiResponseDTO.class);

            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return response.results().get(0).regularMarketPrice();
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar cotação para " + ticker + ": " + e.getMessage());
        }
        return null;
    }
}
