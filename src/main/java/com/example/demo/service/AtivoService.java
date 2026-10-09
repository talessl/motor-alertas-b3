package com.example.demo.service;

import com.example.demo.model.Ativo;
import com.example.demo.repository.AtivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Marca a classe como um serviço do Spring
public class AtivoService {

    // O Spring injeta automaticamente o repositório aqui (Injeção de Dependência)
    @Autowired
    private AtivoRepository ativoRepository;

    public Ativo salvar(Ativo ativo) {
        // Aqui poderíamos adicionar lógicas, como forçar o ticker para MAIÚSCULO
        ativo.setTicker(ativo.getTicker().toUpperCase());
        return ativoRepository.save(ativo);
    }

    public List<Ativo> listarTodos() {
        return ativoRepository.findAll();
    }
}
