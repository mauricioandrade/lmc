package com.example.lmc.service;

import com.example.lmc.dto.LmcFolhaRequestDTO;
import com.example.lmc.entity.*;
import com.example.lmc.repository.BicoRepository;
import com.example.lmc.repository.LmcFolhaRepository;
import com.example.lmc.repository.ProdutoRepository;
import com.example.lmc.repository.TanqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

@Service
public class LmcService {

    @Autowired
    private LmcFolhaRepository lmcFolhaRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private TanqueRepository tanqueRepository;
    @Autowired
    private BicoRepository bicoRepository;

    private static final BigDecimal VARIACAO_PERMITIDA_PERCENTUAL = new BigDecimal("0.6");
    private static final BigDecimal CEM = new BigDecimal("100");

    @Transactional
    public LmcFolha salvarFolhaDiaria(LmcFolhaRequestDTO request) {

        lmcFolhaRepository.findByDataAndProdutoId(request.getData(), request.getProdutoId())
                .ifPresent(f -> {
                    throw new RuntimeException("Já existe uma folha LMC para este produto e data.");
                });

        Produto produto = produtoRepository.findById(request.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        LmcFolha folha = new LmcFolha();
        folha.setData(request.getData());
        folha.setProduto(produto);
        folha.setObservacoes(request.getObservacoes());
        folha.setMedicoesTanque(new ArrayList<>());
        folha.setCompras(new ArrayList<>());
        folha.setVendasBico(new ArrayList<>());


        BigDecimal totalEstoqueAbertura = BigDecimal.ZERO;
        BigDecimal totalEstoqueFechamento = BigDecimal.ZERO;

        for (LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO : request.getMedicoes()) {
            Tanque tanque = tanqueRepository.findById(medicaoDTO.getTanqueId())
                    .orElseThrow(() -> new RuntimeException("Tanque não encontrado"));

            LmcMedicaoTanque medicao = new LmcMedicaoTanque();
            medicao.setLmcFolha(folha);
            medicao.setTanque(tanque);
            medicao.setEstoqueAbertura(medicaoDTO.getEstoqueAbertura());
            medicao.setEstoqueFechamentoFisico(medicaoDTO.getEstoqueFechamentoFisico());

            folha.getMedicoesTanque().add(medicao);

            totalEstoqueAbertura = totalEstoqueAbertura.add(medicao.getEstoqueAbertura());
            totalEstoqueFechamento = totalEstoqueFechamento.add(medicao.getEstoqueFechamentoFisico());
        }
        folha.setEstoqueFechamento(totalEstoqueFechamento); // (Campo 7 / 9.1)


        BigDecimal totalRecebido = BigDecimal.ZERO;
        if (request.getCompras() != null) {
            for (LmcFolhaRequestDTO.CompraDTO compraDTO : request.getCompras()) {
                Tanque tanque = tanqueRepository.findById(compraDTO.getTanqueDescargaId())
                        .orElseThrow(() -> new RuntimeException("Tanque de descarga não encontrado"));

                LmcCompra compra = new LmcCompra();
                compra.setLmcFolha(folha);
                compra.setTanqueDescarga(tanque);
                compra.setNumeroDocumentoFiscal(compraDTO.getNumeroDocumentoFiscal());
                compra.setVolumeRecebido(compraDTO.getVolumeRecebido());

                folha.getCompras().add(compra);

                totalRecebido = totalRecebido.add(compra.getVolumeRecebido());
            }
        }
        folha.setTotalRecebido(totalRecebido);


        BigDecimal volumeDisponivel = totalEstoqueAbertura.add(totalRecebido);
        folha.setVolumeDisponivel(volumeDisponivel);


        BigDecimal totalVendasDia = BigDecimal.ZERO;
        BigDecimal valorVendasDia = BigDecimal.ZERO;

        for (LmcFolhaRequestDTO.VendaBicoDTO vendaDTO : request.getVendas()) {
            Bico bico = bicoRepository.findById(vendaDTO.getBicoId())
                    .orElseThrow(() -> new RuntimeException("Bico não encontrado"));

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

            totalVendasDia = totalVendasDia.add(vendasBicoCalculada);
            valorVendasDia = valorVendasDia.add(
                    vendasBicoCalculada.multiply(venda.getPrecoNaBomba())
            );
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
                if (request.getObservacoes() == null || request.getObservacoes().trim().isEmpty()) {
                    throw new RuntimeException("Variação de estoque superior a 0.6% (" +
                            variacaoPercentual.setScale(3, RoundingMode.HALF_UP) +
                            "%). O campo Observações (13.5) é obrigatório.");
                }
            }
        }


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
}

