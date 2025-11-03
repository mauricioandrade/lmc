package com.example.lmc.service;

import com.example.lmc.dto.LmcFolhaRequestDTO;
import com.example.lmc.entity.*;
import com.example.lmc.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class LmcService {

    private final LmcFolhaRepository lmcFolhaRepository;
    private final ProdutoRepository produtoRepository;
    private final TanqueRepository tanqueRepository;
    private final BicoRepository bicoRepository;
    private final LmcMedicaoTanqueRepository medicaoTanqueRepository;
    private final LmcCompraRepository compraRepository;
    private final LmcVendaBicoRepository vendaBicoRepository;

    private static final BigDecimal VARIACAO_PERMITIDA_PERCENTUAL = new BigDecimal("0.6");
    private static final BigDecimal CEM = new BigDecimal("100");

    @Autowired
    public LmcService(LmcFolhaRepository lmcFolhaRepository, ProdutoRepository produtoRepository,
                      TanqueRepository tanqueRepository, BicoRepository bicoRepository,
                      LmcMedicaoTanqueRepository medicaoTanqueRepository, LmcCompraRepository compraRepository,
                      LmcVendaBicoRepository vendaBicoRepository) {
        this.lmcFolhaRepository = lmcFolhaRepository;
        this.produtoRepository = produtoRepository;
        this.tanqueRepository = tanqueRepository;
        this.bicoRepository = bicoRepository;
        this.medicaoTanqueRepository = medicaoTanqueRepository;
        this.compraRepository = compraRepository;
        this.vendaBicoRepository = vendaBicoRepository;
    }

    @Transactional
    public LmcFolha salvarFolhaDiaria(LmcFolhaRequestDTO request) {

        lmcFolhaRepository.findByDataAndProdutoId(request.getData(), request.getProdutoId())
                .ifPresent(f -> {
                    throw new RuntimeException("Já existe uma folha LMC para este produto e data.");
                });

        Produto produto = produtoRepository.findById(request.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        LmcFolha folha = new LmcFolha();
        folha.setData(request.getData());
        folha.setProduto(produto);
        folha.setObservacoes(request.getObservacoes());
        folha.setMedicoesTanque(new HashSet<>());
        folha.setCompras(new HashSet<>());
        folha.setVendasBico(new HashSet<>());
        for (LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO : request.getMedicoes()) {
            Tanque tanque = tanqueRepository.findById(medicaoDTO.getTanqueId())
                    .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado"));

            LmcMedicaoTanque medicao = new LmcMedicaoTanque();
            medicao.setLmcFolha(folha);
            medicao.setTanque(tanque);
            medicao.setEstoqueAbertura(medicaoDTO.getEstoqueAbertura());
            medicao.setEstoqueFechamentoFisico(medicaoDTO.getEstoqueFechamentoFisico());

            folha.getMedicoesTanque().add(medicao);
        }

        if (request.getCompras() != null) {
            for (LmcFolhaRequestDTO.CompraDTO compraDTO : request.getCompras()) {
                Tanque tanque = tanqueRepository.findById(compraDTO.getTanqueDescargaId())
                        .orElseThrow(() -> new EntityNotFoundException("Tanque de descarga não encontrado"));

                LmcCompra compra = new LmcCompra();
                compra.setLmcFolha(folha);
                compra.setTanqueDescarga(tanque);
                compra.setNumeroDocumentoFiscal(compraDTO.getNumeroDocumentoFiscal());
                compra.setVolumeRecebido(compraDTO.getVolumeRecebido());

                folha.getCompras().add(compra);
            }
        }

        for (LmcFolhaRequestDTO.VendaBicoDTO vendaDTO : request.getVendas()) {
            Bico bico = bicoRepository.findById(vendaDTO.getBicoId())
                    .orElseThrow(() -> new EntityNotFoundException("Bico não encontrado"));

            BigDecimal vendasBicoCalculada = new BigDecimal(vendaDTO.getEncerranteFechamento())
                    .subtract(new BigDecimal(vendaDTO.getEncerranteAbertura()))
                    .subtract(vendaDTO.getAfericoes());

            LmcVendaBico venda = new LmcVendaBico();
            venda.setLmcFolha(folha);
            venda.setBico(bico);
            venda.setPrecoNaBomba(vendaDTO.getPrecoNaBomba());
            venda.setEncerranteAbertura(vendaDTO.getEncerranteAbertura());
            venda.setEncerranteFechamento(vendaDTO.getEncerranteFechamento());
            venda.setAfericoes(vendaDTO.getAfericoes());
            venda.setVendasBico(vendasBicoCalculada);

            folha.getVendasBico().add(venda);
        }

        recalcularEValidarTotais(folha);

        BigDecimal acumuladoAnterior = lmcFolhaRepository
                .findTopByProdutoIdAndDataBeforeOrderByDataDesc(produto.getId(), folha.getData())
                .map(folhaAnterior -> {
                    if (folhaAnterior.getData().getMonth().equals(folha.getData().getMonth())) {
                        return folhaAnterior.getValorAcumuladoMes();
                    }
                    return BigDecimal.ZERO;
                })
                .orElse(BigDecimal.ZERO);

        folha.setValorAcumuladoMes(
                acumuladoAnterior.add(folha.getValorVendasDia())
        );

        return lmcFolhaRepository.save(folha);
    }

    @Transactional(readOnly = true)
    public LmcFolha buscarFolhaPorDataEProduto(LocalDate data, Long produtoId) {
        return lmcFolhaRepository.findByDataAndProdutoIdEager(data, produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Nenhuma folha LMC encontrada para esta data e produto."));
    }

    @Transactional
    public LmcMedicaoTanque atualizarMedicaoTanque(Long id, LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {
        LmcMedicaoTanque medicaoExistente = medicaoTanqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medição não encontrada: " + id));

        medicaoExistente.setEstoqueAbertura(medicaoDTO.getEstoqueAbertura());
        medicaoExistente.setEstoqueFechamentoFisico(medicaoDTO.getEstoqueFechamentoFisico());

        if (medicaoDTO.getTanqueId() != null) {
            Tanque tanque = tanqueRepository.findById(medicaoDTO.getTanqueId())
                    .orElseThrow(() -> new RuntimeException("Tanque não encontrado"));
            medicaoExistente.setTanque(tanque);
        }

        LmcMedicaoTanque medicaoSalva = medicaoTanqueRepository.save(medicaoExistente);
        recalcularEValidarTotais(medicaoSalva.getLmcFolha());
        return medicaoSalva;
    }

    @Transactional
    public LmcCompra atualizarCompra(Long id, LmcFolhaRequestDTO.CompraDTO compraDTO) {
        LmcCompra compraExistente = compraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra não encontrada: " + id));

        compraExistente.setNumeroDocumentoFiscal(compraDTO.getNumeroDocumentoFiscal());
        compraExistente.setVolumeRecebido(compraDTO.getVolumeRecebido());

        if (compraDTO.getTanqueDescargaId() != null) {
            Tanque tanque = tanqueRepository.findById(compraDTO.getTanqueDescargaId())
                    .orElseThrow(() -> new RuntimeException("Tanque de descarga não encontrado"));
            compraExistente.setTanqueDescarga(tanque);
        }

        LmcCompra compraSalva = compraRepository.save(compraExistente);
        recalcularEValidarTotais(compraSalva.getLmcFolha());
        return compraSalva;
    }

    @Transactional
    public LmcVendaBico atualizarVenda(Long id, LmcFolhaRequestDTO.VendaBicoDTO vendaDTO) {
        LmcVendaBico vendaExistente = vendaBicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada: " + id));

        vendaExistente.setEncerranteAbertura(vendaDTO.getEncerranteAbertura());
        vendaExistente.setEncerranteFechamento(vendaDTO.getEncerranteFechamento());
        vendaExistente.setAfericoes(vendaDTO.getAfericoes());
        vendaExistente.setPrecoNaBomba(vendaDTO.getPrecoNaBomba());

        BigDecimal vendasBicoCalculada = new BigDecimal(vendaDTO.getEncerranteFechamento())
                .subtract(new BigDecimal(vendaDTO.getEncerranteAbertura()))
                .subtract(vendaDTO.getAfericoes());
        vendaExistente.setVendasBico(vendasBicoCalculada);

        if (vendaDTO.getBicoId() != null) {
            Bico bico = bicoRepository.findById(vendaDTO.getBicoId())
                    .orElseThrow(() -> new RuntimeException("Bico não encontrado"));
            vendaExistente.setBico(bico);
        }

        LmcVendaBico vendaSalva = vendaBicoRepository.save(vendaExistente);
        recalcularEValidarTotais(vendaSalva.getLmcFolha());
        return vendaSalva;
    }

    @Transactional
    public LmcMedicaoTanque adicionarMedicaoTanque(Long folhaId, LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {
        LmcFolha folha = lmcFolhaRepository.findById(folhaId)
                .orElseThrow(() -> new EntityNotFoundException("Folha LMC não encontrada: " + folhaId));

        Tanque tanque = tanqueRepository.findById(medicaoDTO.getTanqueId())
                .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado"));

        LmcMedicaoTanque novaMedicao = new LmcMedicaoTanque();
        novaMedicao.setLmcFolha(folha);
        novaMedicao.setTanque(tanque);
        novaMedicao.setEstoqueAbertura(medicaoDTO.getEstoqueAbertura());
        novaMedicao.setEstoqueFechamentoFisico(medicaoDTO.getEstoqueFechamentoFisico());

        LmcMedicaoTanque medicaoSalva = medicaoTanqueRepository.save(novaMedicao);

        folha.getMedicoesTanque().add(medicaoSalva);

        recalcularEValidarTotais(folha);

        return medicaoSalva;
    }

    @Transactional
    public void deletarMedicaoTanque(Long id) {
        LmcMedicaoTanque medicao = medicaoTanqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medição não encontrada: " + id));
        LmcFolha folha = medicao.getLmcFolha();

        folha.getMedicoesTanque().remove(medicao);
        medicaoTanqueRepository.delete(medicao);

        recalcularEValidarTotais(folha);
    }

    @Transactional
    public void deletarCompra(Long id) {
        LmcCompra compra = compraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra não encontrada: " + id));
        LmcFolha folha = compra.getLmcFolha();

        folha.getCompras().remove(compra);
        compraRepository.delete(compra);

        recalcularEValidarTotais(folha);
    }

    @Transactional
    public void deletarVenda(Long id) {
        LmcVendaBico venda = vendaBicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada: " + id));
        LmcFolha folha = venda.getLmcFolha();

        folha.getVendasBico().remove(venda);
        vendaBicoRepository.delete(venda);

        recalcularEValidarTotais(folha);
    }

    private void recalcularEValidarTotais(LmcFolha folha) {

        BigDecimal totalEstoqueAbertura = BigDecimal.ZERO;
        BigDecimal totalEstoqueFechamento = BigDecimal.ZERO;

        Set<LmcMedicaoTanque> medicoes = medicaoTanqueRepository.findByLmcFolhaId(folha.getId());
        for (LmcMedicaoTanque medicao : medicoes) {
            totalEstoqueAbertura = totalEstoqueAbertura.add(medicao.getEstoqueAbertura());
            totalEstoqueFechamento = totalEstoqueFechamento.add(medicao.getEstoqueFechamentoFisico());
        }
        folha.setEstoqueFechamento(totalEstoqueFechamento);

        BigDecimal totalRecebido = BigDecimal.ZERO;
        Set<LmcCompra> compras = compraRepository.findByLmcFolhaId(folha.getId());
        if (compras != null) {
            for (LmcCompra compra : compras) {
                totalRecebido = totalRecebido.add(compra.getVolumeRecebido());
            }
        }
        folha.setTotalRecebido(totalRecebido);

        BigDecimal volumeDisponivel = totalEstoqueAbertura.add(totalRecebido);
        folha.setVolumeDisponivel(volumeDisponivel);

        BigDecimal totalVendasDia = BigDecimal.ZERO;
        BigDecimal valorVendasDia = BigDecimal.ZERO;
        Set<LmcVendaBico> vendas = vendaBicoRepository.findByLmcFolhaId(folha.getId());

        if (vendas != null) {
            for (LmcVendaBico venda : vendas) {
                BigDecimal vendasBicoCalculada = new BigDecimal(venda.getEncerranteFechamento())
                        .subtract(new BigDecimal(venda.getEncerranteAbertura()))
                        .subtract(venda.getAfericoes());

                venda.setVendasBico(vendasBicoCalculada);

                totalVendasDia = totalVendasDia.add(vendasBicoCalculada);
                valorVendasDia = valorVendasDia.add(
                        vendasBicoCalculada.multiply(venda.getPrecoNaBomba())
                );
            }
        }
        folha.setTotalVendasDia(totalVendasDia);
        folha.setValorVendasDia(valorVendasDia.setScale(2, RoundingMode.HALF_UP));

        BigDecimal estoqueEscritural = volumeDisponivel.subtract(totalVendasDia);
        folha.setEstoqueEscritural(estoqueEscritural);

        BigDecimal perdasGanhos = totalEstoqueFechamento.subtract(estoqueEscritural);
        folha.setPerdasGanhos(perdasGanhos);

        if (volumeDisponivel.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variacaoPercentual = perdasGanhos.abs()
                    .divide(volumeDisponivel, 6, RoundingMode.HALF_UP)
                    .multiply(CEM);

            if (variacaoPercentual.compareTo(VARIACAO_PERMITIDA_PERCENTUAL) > 0) {
                if (folha.getObservacoes() == null || folha.getObservacoes().trim().isEmpty()) {
                    if (folha.getId() == null) {
                        throw new RuntimeException("Variação de estoque superior a 0.6% (" +
                                variacaoPercentual.setScale(3, RoundingMode.HALF_UP) +
                                "%). O campo Observações (13.5) é obrigatório.");
                    }
                }
            }
        }

        lmcFolhaRepository.save(folha);
    }
}
