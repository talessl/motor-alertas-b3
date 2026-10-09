package com.example.demo.repository;

import com.example.demo.model.Ativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {
    // Apenas criando essa interface, o Spring Boot automaticamente
    // gera comandos de INSERT, UPDATE, DELETE e SELECT por baixo dos panos!
    Optional<Ativo> findByTicker(String ticker);
}