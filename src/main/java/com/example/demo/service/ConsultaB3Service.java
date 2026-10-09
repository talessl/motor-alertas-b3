package com.example.demo.service;

import com.example.demo.dto.BrapiResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class ConsultaB3Service {

    private static final Logger log = LoggerFactory.getLogger(ConsultaB3Service.class);
    private static final String URL = "https://brapi.dev/api/quote/{ticker}?token={token}";

    private final String brapiToken;
    private final RestTemplate restTemplate;

    public ConsultaB3Service(@Value("${brapi.token}") String brapiToken) {
        this.brapiToken = brapiToken;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restTemplate = new RestTemplate(factory);
    }

    public Double buscarPrecoReal(String ticker) {
        try {
            BrapiResponseDTO response =
                    restTemplate.getForObject(URL, BrapiResponseDTO.class, ticker, brapiToken);

            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return response.results().get(0).regularMarketPrice();
            }
        } catch (HttpStatusCodeException e) {
            log.warn("Falha ao buscar cotação de {}: HTTP {}", ticker, e.getStatusCode().value());
        } catch (Exception e) {
            log.warn("Falha ao buscar cotação de {}: {}", ticker, e.getClass().getSimpleName());
        }
        return null;
    }
}