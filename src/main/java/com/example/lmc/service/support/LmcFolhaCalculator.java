package com.example.lmc.service.support;

import com.example.lmc.entity.LmcCompra;
import com.example.lmc.entity.LmcFolha;
import com.example.lmc.entity.LmcMedicaoTanque;
import com.example.lmc.entity.LmcVendaBico;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;

@Component
public class LmcFolhaCalculator {

    public void atualizarTotais(LmcFolha folha) {
        Set<LmcMedicaoTanque> medicoes = safeSet(folha.getMedicoesTanque());
        BigDecimal totalEstoqueAbertura = medicoes.stream()
                .map(LmcMedicaoTanque::getEstoqueAbertura)
                .map(this::valorOuZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEstoqueFechamento = medicoes.stream()
                .map(LmcMedicaoTanque::getEstoqueFechamentoFisico)
                .map(this::valorOuZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        folha.setEstoqueFechamento(totalEstoqueFechamento);

        Set<LmcCompra> compras = safeSet(folha.getCompras());
        BigDecimal totalRecebido = compras.stream()
                .map(LmcCompra::getVolumeRecebido)
                .map(this::valorOuZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        folha.setTotalRecebido(totalRecebido);

        BigDecimal volumeDisponivel = totalEstoqueAbertura.add(totalRecebido);
        folha.setVolumeDisponivel(volumeDisponivel);

        Set<LmcVendaBico> vendas = safeSet(folha.getVendasBico());
        BigDecimal totalVendasDia = BigDecimal.ZERO;
        BigDecimal valorVendasDia = BigDecimal.ZERO;
        for (LmcVendaBico venda : vendas) {
            BigDecimal vendasCalculadas = calcularVolumeVendido(
                    venda.getEncerranteFechamento(),
                    venda.getEncerranteAbertura(),
                    venda.getAfericoes()
            );
            venda.setVendasBico(vendasCalculadas);
            totalVendasDia = totalVendasDia.add(vendasCalculadas);
            valorVendasDia = valorVendasDia.add(vendasCalculadas.multiply(valorOuZero(venda.getPrecoNaBomba())));
        }
        folha.setTotalVendasDia(totalVendasDia);
        folha.setValorVendasDia(valorVendasDia.setScale(2, RoundingMode.HALF_UP));

        BigDecimal estoqueEscritural = volumeDisponivel.subtract(totalVendasDia);
        folha.setEstoqueEscritural(estoqueEscritural);

        BigDecimal perdasGanhos = totalEstoqueFechamento.subtract(estoqueEscritural);
        folha.setPerdasGanhos(perdasGanhos);
    }

    public BigDecimal calcularVolumeVendido(BigDecimal encerranteFechamento, BigDecimal encerranteAbertura, BigDecimal afericoes) {
        BigDecimal fechamento = valorOuZero(encerranteFechamento);
        BigDecimal abertura = valorOuZero(encerranteAbertura);
        BigDecimal afericao = valorOuZero(afericoes);
        return fechamento.subtract(abertura).subtract(afericao);
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private <T> Set<T> safeSet(Set<T> conjunto) {
        return conjunto == null ? Collections.emptySet() : conjunto;
    }
}
