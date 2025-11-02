package com.example.lmc.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "lmc_folha", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"data", "produto_id"})
})

public class LmcFolha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @OneToMany(mappedBy = "lmcFolha", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("folha-medicao")
    private List<LmcMedicaoTanque> medicoesTanque;

    @OneToMany(mappedBy = "lmcFolha", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("folha-compra")
    private List<LmcCompra> compras;

    @OneToMany(mappedBy = "lmcFolha", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("folha-venda")
    private List<LmcVendaBico> vendasBico;

    // --- Campos Calculados/Totalizados ---

    @Column(name = "total_recebido", precision = 19, scale = 3)
    private BigDecimal totalRecebido;

    @Column(name = "volume_disponivel", precision = 19, scale = 3)
    private BigDecimal volumeDisponivel;

    @Column(name = "total_vendas_dia", precision = 19, scale = 3)
    private BigDecimal totalVendasDia;

    @Column(name = "estoque_escritural", precision = 19, scale = 3)
    private BigDecimal estoqueEscritural;

    @Column(name = "estoque_fechamento", precision = 19, scale = 3)
    private BigDecimal estoqueFechamento;

    @Column(name = "perdas_ganhos", precision = 19, scale = 3)
    private BigDecimal perdasGanhos;

    @Column(name = "valor_vendas_dia", precision = 19, scale = 2)
    private BigDecimal valorVendasDia;

    @Column(name = "valor_acumulado_mes", precision = 19, scale = 2)
    private BigDecimal valorAcumuladoMes;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;
}
