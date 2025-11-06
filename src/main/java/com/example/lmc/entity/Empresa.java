package com.example.lmc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.br.CNPJ;

@Entity
@Table(name = "tb_empresa")
@Data
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String razaoSocial;

    @NotBlank
    @CNPJ
    @Column(nullable = false, unique = true)
    private String cnpj;


    @Size(max = 255)
    private String inscricaoEstadual;

    @Size(max = 255)
    private String enderecoCompleto;

    @Column(nullable = false)
    private Boolean isAtiva = false;
}

