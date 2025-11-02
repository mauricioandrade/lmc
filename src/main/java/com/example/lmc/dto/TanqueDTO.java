package com.example.lmc.dto;

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
}
