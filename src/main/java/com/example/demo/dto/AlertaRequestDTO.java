package com.example.demo.dto;

import com.example.demo.model.Condicao;

import java.math.BigDecimal;

public record AlertaRequestDTO(String ticker, Condicao condicao, BigDecimal valorAlvo) {}
