package com.example.lmc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "lmc_medicao_tanque")
public class LmcMedicaoTanque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lmc_folha_id", nullable = false)
    @JsonBackReference("folha-medicao")
    private LmcFolha lmcFolha;

    @ManyToOne
    @JoinColumn(name = "tanque_id", nullable = false)
    private Tanque tanque;

    @Column(name = "estoque_abertura", nullable = false, precision = 19, scale = 3)
    private BigDecimal estoqueAbertura;

    @Column(name = "estoque_fechamento_fisico", nullable = false, precision = 19, scale = 3)
    private BigDecimal estoqueFechamentoFisico;
}

