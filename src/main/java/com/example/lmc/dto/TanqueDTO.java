package com.example.lmc.dto;

import com.example.lmc.entity.Tanque;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TanqueDTO {
    private Long id;
    private String numero;
    private BigDecimal capacidadeNominal;


    private Long produtoId;
    private String produtoNome;

    public TanqueDTO(Tanque tanque) {
        this.id = tanque.getId();
        this.numero = tanque.getNumero();
        this.capacidadeNominal = tanque.getCapacidadeNominal();
        if (tanque.getProduto() != null) {
            this.produtoId = tanque.getProduto().getId();
            this.produtoNome = tanque.getProduto().getNome();
        }
    }

    public TanqueDTO(Long id, String numero, BigDecimal capacidadeNominal) {
    }
}
