package com.example.demo.repository;

import com.example.demo.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByAtivoTickerAndHabilitadoTrue(String ticker);
}
