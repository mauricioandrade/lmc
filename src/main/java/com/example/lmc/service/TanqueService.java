package com.example.lmc.service;

import com.example.lmc.dto.TanqueDTO;
import com.example.lmc.entity.Produto;
import com.example.lmc.entity.Tanque;
import com.example.lmc.repository.BicoRepository;
import com.example.lmc.repository.ProdutoRepository;
import com.example.lmc.repository.TanqueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TanqueService {

    private final TanqueRepository tanqueRepository;
    private final ProdutoRepository produtoRepository;
    private final BicoRepository bicoRepository; // <-- 2. Injete o BicoRepository

    @Autowired
    public TanqueService(TanqueRepository tanqueRepository, ProdutoRepository produtoRepository, BicoRepository bicoRepository) {
        this.tanqueRepository = tanqueRepository;
        this.produtoRepository = produtoRepository;
        this.bicoRepository = bicoRepository; // <-- 3. Adicione ao construtor
    }

    @Transactional(readOnly = true)
    public List<TanqueDTO> listarTodos() {
        return tanqueRepository.findAllWithProduto().stream()
                .map(TanqueDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Tanque buscarEntidadePorId(Long id) {
        return tanqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado com id: " + id));
    }

    public TanqueDTO salvarTanque(TanqueDTO tanqueDTO) {

        tanqueRepository.findByNumero(tanqueDTO.getNumero()).ifPresent(t -> {
            throw new RuntimeException("Já existe um tanque com o número: " + tanqueDTO.getNumero());
        });


        Produto produto = produtoRepository.findById(tanqueDTO.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com id: " + tanqueDTO.getProdutoId()));


        Tanque novoTanque = new Tanque();
        novoTanque.setNumero(tanqueDTO.getNumero());
        novoTanque.setCapacidadeNominal(tanqueDTO.getCapacidadeNominal());
        novoTanque.setProduto(produto);

        Tanque tanqueSalvo = tanqueRepository.save(novoTanque);
        return new TanqueDTO(tanqueSalvo);
    }

    public TanqueDTO atualizarTanque(Long id, TanqueDTO tanqueDTO) {

        Tanque tanqueExistente = buscarEntidadePorId(id);


        tanqueRepository.findByNumero(tanqueDTO.getNumero()).ifPresent(t -> {
            if (!t.getId().equals(id)) {
                throw new RuntimeException("Já existe outro tanque com o número: " + tanqueDTO.getNumero());
            }
        });


        Produto produto = produtoRepository.findById(tanqueDTO.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com id: " + tanqueDTO.getProdutoId()));


        tanqueExistente.setNumero(tanqueDTO.getNumero());
        tanqueExistente.setCapacidadeNominal(tanqueDTO.getCapacidadeNominal());
        tanqueExistente.setProduto(produto);

        Tanque tanqueSalvo = tanqueRepository.save(tanqueExistente);
        return new TanqueDTO(tanqueSalvo);
    }

    public void deletarTanque(Long id) {
        Tanque tanque = buscarEntidadePorId(id);


        if (bicoRepository.existsByTanqueId(id)) {
            throw new RuntimeException("Não é possível excluir o tanque pois ele possui bicos associados.");
        }

        // TODO: Adicionar validação para LmcMedicaoTanque e LmcCompra

        tanqueRepository.delete(tanque);
    }
}
