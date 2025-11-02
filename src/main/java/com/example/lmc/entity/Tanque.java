package com.example.lmc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@Table(name = "tb_tanque")
public class Tanque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true)
    private String numero;

    @Column(name = "capacidade_nominal", nullable = false, precision = 19, scale = 3)
    private BigDecimal capacidadeNominal;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonBackReference("produto-tanque")
    private Produto produto;

    @OneToMany(mappedBy = "tanque")
    @JsonManagedReference("tanque-bico")
    private List<Bico> bicos;


}
