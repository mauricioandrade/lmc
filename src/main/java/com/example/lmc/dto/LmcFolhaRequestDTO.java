package com.example.lmc.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class LmcFolhaRequestDTO {

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    @NotNull(message = "ID do Produto é obrigatório")
    private Long produtoId;

    private String observacoes;

    @NotEmpty(message = "Deve haver pelo menos uma medição de tanque")
    private List<MedicaoTanqueDTO> medicoes;

    @NotEmpty(message = "Deve haver pelo menos uma venda de bico")
    private List<VendaBicoDTO> vendas;

    private List<CompraDTO> compras;


    @Data
    public static class MedicaoTanqueDTO {
        @NotNull
        private Long tanqueId;
        @NotNull
        private BigDecimal estoqueAbertura;
        @NotNull
        private BigDecimal estoqueFechamentoFisico;
    }

    @Data
    public static class VendaBicoDTO {
        @NotNull
        private Long bicoId;
        @NotNull
        private BigDecimal precoNaBomba;
        @NotNull
        private Long encerranteAbertura;
        @NotNull
        private Long encerranteFechamento;
        @NotNull
        private BigDecimal afericoes;
    }

    @Data
    public static class CompraDTO {
        @NotNull
        private Long tanqueDescargaId;
        @NotNull
        private String numeroDocumentoFiscal;
        @NotNull
        private BigDecimal volumeRecebido;
    }
}

