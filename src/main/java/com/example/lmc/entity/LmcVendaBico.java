package com.example.lmc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "lmc_venda_bico")
public class LmcVendaBico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lmc_folha_id", nullable = false)
    private LmcFolha lmcFolha;

    @ManyToOne
    @JoinColumn(name = "bico_id", nullable = false)
    private Bico bico;

    @Column(name = "preco_na_bomba", nullable = false, precision = 19, scale = 3)
    private BigDecimal precoNaBomba;

    @Column(name = "encerrante_abertura", nullable = false)
    private Long encerranteAbertura;

    @Column(name = "encerrante_fechamento", nullable = false)
    private Long encerranteFechamento;

    @Column(name = "afericoes", precision = 19, scale = 3)
    private BigDecimal afericoes;

    @Column(name = "vendas_bico", nullable = false, precision = 19, scale = 3)
    private BigDecimal vendasBico;

}
