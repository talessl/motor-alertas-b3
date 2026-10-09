package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data // Anotação do Lombok que cria getters e setters invisivelmente
@Entity // Diz ao Spring que isso é uma tabela no banco
@Table(name = "ativos")
public class Ativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 6)
    private String ticker; // Ex: PETR4, VALE3

    private Double ultimoPreco;
}
