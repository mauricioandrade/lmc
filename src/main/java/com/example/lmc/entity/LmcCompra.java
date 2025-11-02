package com.example.lmc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "lmc_compra")
public class LmcCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lmc_folha_id", nullable = false)
    @JsonBackReference("folha-compra")
    private LmcFolha lmcFolha;

    @ManyToOne
    @JoinColumn(name = "tanque_descarga_id", nullable = false)
    private Tanque tanqueDescarga;

    @Column(name = "numero_documento_fiscal", nullable = false)
    private String numeroDocumentoFiscal;

    @Column(name = "volume_recebido", nullable = false, precision = 19, scale = 3)
    private BigDecimal volumeRecebido;
}
