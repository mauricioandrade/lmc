package com.example.lmc.service;

import com.example.lmc.dto.BicoDTO;
import com.example.lmc.entity.Bico;
import com.example.lmc.entity.Tanque;
import com.example.lmc.repository.BicoRepository;
import com.example.lmc.repository.LmcVendaBicoRepository; // <-- 1. Importe o repo de Venda
import com.example.lmc.repository.TanqueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BicoService {

    private final BicoRepository bicoRepository;
    private final TanqueRepository tanqueRepository;
    private final LmcVendaBicoRepository lmcVendaBicoRepository; // <-- 2. Injete o repo de Venda

    @Autowired
    public BicoService(BicoRepository bicoRepository, TanqueRepository tanqueRepository, LmcVendaBicoRepository lmcVendaBicoRepository) {
        this.bicoRepository = bicoRepository;
        this.tanqueRepository = tanqueRepository;
        this.lmcVendaBicoRepository = lmcVendaBicoRepository; // <-- 3. Adicione ao construtor
    }

    @Transactional(readOnly = true)
    public List<BicoDTO> listarTodos() {
        return bicoRepository.findAllWithTanqueAndProduto().stream()
                .map(BicoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Bico buscarEntidadePorId(Long id) {
        return bicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bico não encontrado com id: " + id));
    }

    public BicoDTO salvarBico(BicoDTO bicoDTO) {
        // 1. Valida se o número já existe
        bicoRepository.findByNumero(bicoDTO.getNumero()).ifPresent(b -> {
            throw new RuntimeException("Já existe um bico com o número: " + bicoDTO.getNumero());
        });

        // 2. Associa o Tanque
        Tanque tanque = tanqueRepository.findById(bicoDTO.getTanqueId())
                .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado com id: " + bicoDTO.getTanqueId()));

        // 3. Converte DTO para Entidade
        Bico novoBico = new Bico();
        novoBico.setNumero(bicoDTO.getNumero());
        novoBico.setTanque(tanque);

        Bico bicoSalvo = bicoRepository.save(novoBico);
        return new BicoDTO(bicoSalvo);
    }

    public BicoDTO atualizarBico(Long id, BicoDTO bicoDTO) {
        // 1. Busca o bico existente
        Bico bicoExistente = buscarEntidadePorId(id);

        // 2. Valida se o novo número já está em uso por *outro* bico
        bicoRepository.findByNumero(bicoDTO.getNumero()).ifPresent(b -> {
            if (!b.getId().equals(id)) {
                throw new RuntimeException("Já existe outro bico com o número: " + bicoDTO.getNumero());
            }
        });

        // 3. Associa o novo Tanque
        Tanque tanque = tanqueRepository.findById(bicoDTO.getTanqueId())
                .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado com id: " + bicoDTO.getTanqueId()));

        // 4. Atualiza os dados
        bicoExistente.setNumero(bicoDTO.getNumero());
        bicoExistente.setTanque(tanque);

        Bico bicoSalvo = bicoRepository.save(bicoExistente);
        return new BicoDTO(bicoSalvo);
    }

    public void deletarBico(Long id) {
        Bico bico = buscarEntidadePorId(id);

        // 4. Validação de segurança: Não pode excluir se tiver vendas associadas
        if (lmcVendaBicoRepository.existsByBicoId(id)) {
            throw new RuntimeException("Não é possível excluir o bico pois ele possui vendas associadas no LMC.");
        }

        bicoRepository.delete(bico);
    }
}
