package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ativo_id")
    private Ativo ativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condicao condicao;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal valorAlvo;

    @Column(nullable = false)
    private boolean habilitado = true;

    private LocalDateTime ultimoDisparo;
}