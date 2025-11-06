package com.example.lmc.service;

import com.example.lmc.dto.BicoDTO;
import com.example.lmc.entity.Bico;
import com.example.lmc.entity.Tanque;
import com.example.lmc.exception.BusinessException;
import com.example.lmc.repository.BicoRepository;
import com.example.lmc.repository.LmcVendaBicoRepository;
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
    private final LmcVendaBicoRepository lmcVendaBicoRepository;

    @Autowired
    public BicoService(BicoRepository bicoRepository, TanqueRepository tanqueRepository, LmcVendaBicoRepository lmcVendaBicoRepository) {
        this.bicoRepository = bicoRepository;
        this.tanqueRepository = tanqueRepository;
        this.lmcVendaBicoRepository = lmcVendaBicoRepository;
    }

    @Transactional(readOnly = true)
    public List<BicoDTO> listarTodos() {
        return bicoRepository.findAllWithTanqueAndProduto().stream()
                .map(BicoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BicoDTO> listarPorTanque(Long tanqueId) {
        return bicoRepository.findByTanqueId(tanqueId).stream()
                .filter(bico -> bico.getTanque() != null)
                .map(BicoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Bico buscarEntidadePorId(Long id) {
        return bicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bico não encontrado com id: " + id));
    }

    public BicoDTO salvarBico(BicoDTO bicoDTO) {
        validarNumeroUnico(bicoDTO.getNumero(), null);
        Tanque tanque = buscarTanque(bicoDTO.getTanqueId());
        Bico novoBico = new Bico();
        novoBico.setNumero(bicoDTO.getNumero());
        novoBico.setTanque(tanque);

        Bico bicoSalvo = bicoRepository.save(novoBico);
        return new BicoDTO(bicoSalvo);
    }

    public BicoDTO atualizarBico(Long id, BicoDTO bicoDTO) {
        Bico bicoExistente = buscarEntidadePorId(id);
        validarNumeroUnico(bicoDTO.getNumero(), id);
        Tanque tanque = buscarTanque(bicoDTO.getTanqueId());
        bicoExistente.setNumero(bicoDTO.getNumero());
        bicoExistente.setTanque(tanque);

        Bico bicoSalvo = bicoRepository.save(bicoExistente);
        return new BicoDTO(bicoSalvo);
    }

    public void deletarBico(Long id) {
        Bico bico = buscarEntidadePorId(id);
        if (lmcVendaBicoRepository.existsByBicoId(id)) {
            throw new BusinessException("Não é possível excluir o bico pois ele possui vendas associadas no LMC.");
        }
        bicoRepository.delete(bico);
    }

    private void validarNumeroUnico(String numero, Long idAtual) {
        bicoRepository.findByNumero(numero).ifPresent(bico -> {
            boolean mesmoRegistro = idAtual != null && bico.getId().equals(idAtual);
            if (!mesmoRegistro) {
                throw new BusinessException("Já existe um bico com o número: " + numero);
            }
        });
    }

    private Tanque buscarTanque(Long tanqueId) {
        return tanqueRepository.findById(tanqueId)
                .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado com id: " + tanqueId));
    }
}
