package com.example.lmc.service.support;

import com.example.lmc.dto.LmcFolhaRequestDTO;
import com.example.lmc.entity.Bico;
import com.example.lmc.entity.LmcCompra;
import com.example.lmc.entity.LmcFolha;
import com.example.lmc.entity.LmcMedicaoTanque;
import com.example.lmc.entity.LmcVendaBico;
import com.example.lmc.entity.Produto;
import com.example.lmc.entity.Tanque;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;

@Component
public class LmcFolhaFactory {

    public LmcFolha criarFolha(LmcFolhaRequestDTO request, Produto produto) {
        LmcFolha folha = new LmcFolha();
        folha.setData(request.getData());
        folha.setProduto(produto);
        folha.setObservacoes(request.getObservacoes());
        folha.setMedicoesTanque(new HashSet<>());
        folha.setCompras(new HashSet<>());
        folha.setVendasBico(new HashSet<>());
        return folha;
    }

    public LmcMedicaoTanque criarMedicao(LmcFolha folha, LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO, Tanque tanque) {
        LmcMedicaoTanque medicao = new LmcMedicaoTanque();
        medicao.setLmcFolha(folha);
        medicao.setTanque(tanque);
        medicao.setEstoqueAbertura(medicaoDTO.getEstoqueAbertura());
        medicao.setEstoqueFechamentoFisico(medicaoDTO.getEstoqueFechamentoFisico());
        return medicao;
    }

    public LmcCompra criarCompra(LmcFolha folha, LmcFolhaRequestDTO.CompraDTO compraDTO, Tanque tanqueDescarga) {
        LmcCompra compra = new LmcCompra();
        compra.setLmcFolha(folha);
        compra.setTanqueDescarga(tanqueDescarga);
        compra.setNumeroDocumentoFiscal(compraDTO.getNumeroDocumentoFiscal());
        compra.setVolumeRecebido(compraDTO.getVolumeRecebido());
        return compra;
    }

    public LmcVendaBico criarVenda(LmcFolha folha, LmcFolhaRequestDTO.VendaBicoDTO vendaDTO, Bico bico, BigDecimal volumeVendido) {
        LmcVendaBico venda = new LmcVendaBico();
        venda.setLmcFolha(folha);
        venda.setBico(bico);
        venda.setPrecoNaBomba(vendaDTO.getPrecoNaBomba());
        venda.setEncerranteAbertura(vendaDTO.getEncerranteAbertura());
        venda.setEncerranteFechamento(vendaDTO.getEncerranteFechamento());
        venda.setAfericoes(vendaDTO.getAfericoes());
        venda.setVendasBico(volumeVendido);
        return venda;
    }
}
