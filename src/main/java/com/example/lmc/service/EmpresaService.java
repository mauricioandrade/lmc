package com.example.lmc.service;

import com.example.lmc.dto.EmpresaDTO;
import com.example.lmc.entity.Empresa;
import com.example.lmc.exception.BusinessException;
import com.example.lmc.repository.EmpresaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpresaDTO> listarTodas() {
        return empresaRepository.findAllOrderByRazaoSocial().stream()
                .map(EmpresaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Empresa buscarEntidadePorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada com id: " + id));
    }

    @Transactional
    public void desativarTodasOutrasEmpresas(Long idEmpresaAtiva) {
        List<Empresa> todasEmpresas = empresaRepository.findAll();
        for (Empresa emp : todasEmpresas) {
            if (!emp.getId().equals(idEmpresaAtiva)) {
                emp.setIsAtiva(false);
                empresaRepository.save(emp);
            }
        }
    }

    public EmpresaDTO salvarEmpresa(EmpresaDTO empresaDTO) {
        empresaRepository.findByCnpj(empresaDTO.getCnpj()).ifPresent(e -> {
            throw new BusinessException("Já existe uma empresa com o CNPJ: " + empresaDTO.getCnpj());
        });

        Empresa novaEmpresa = new Empresa();
        novaEmpresa.setRazaoSocial(empresaDTO.getRazaoSocial());
        novaEmpresa.setCnpj(empresaDTO.getCnpj());
        novaEmpresa.setInscricaoEstadual(empresaDTO.getInscricaoEstadual());
        novaEmpresa.setEnderecoCompleto(empresaDTO.getEnderecoCompleto());
        novaEmpresa.setIsAtiva(empresaDTO.getIsAtiva());

        Empresa empresaSalva = empresaRepository.save(novaEmpresa);
        if (empresaSalva.getIsAtiva()) {
            desativarTodasOutrasEmpresas(empresaSalva.getId());
        }

        return new EmpresaDTO(empresaSalva);
    }

    public EmpresaDTO atualizarEmpresa(Long id, EmpresaDTO empresaDTO) {
        Empresa empresaExistente = buscarEntidadePorId(id);

        empresaRepository.findByCnpj(empresaDTO.getCnpj()).ifPresent(e -> {
            if (!e.getId().equals(id)) {
                throw new BusinessException("Já existe outra empresa com o CNPJ: " + empresaDTO.getCnpj());
            }
        });

        empresaExistente.setRazaoSocial(empresaDTO.getRazaoSocial());
        empresaExistente.setCnpj(empresaDTO.getCnpj());
        empresaExistente.setInscricaoEstadual(empresaDTO.getInscricaoEstadual());
        empresaExistente.setEnderecoCompleto(empresaDTO.getEnderecoCompleto());
        empresaExistente.setIsAtiva(empresaDTO.getIsAtiva());

        Empresa empresaSalva = empresaRepository.save(empresaExistente);

        if (empresaSalva.getIsAtiva()) {
            desativarTodasOutrasEmpresas(empresaSalva.getId());
        }

        return new EmpresaDTO(empresaSalva);
    }

    public void deletarEmpresa(Long id) {
        Empresa empresa = buscarEntidadePorId(id);
        empresaRepository.delete(empresa);
    }

    @Transactional(readOnly = true)
    public Empresa getEmpresaAtiva() {
        return empresaRepository.findFirstByIsAtivaTrue()
                .orElseThrow(() -> new EntityNotFoundException("Nenhuma empresa está marcada como 'Ativa' no sistema."));
    }
}
