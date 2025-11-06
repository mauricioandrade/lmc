package com.example.lmc.dto;

import com.example.lmc.entity.Bico; // <-- 1. Adicione este import
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BicoDTO {
    private Long id;
    private String numero;

    // --- 2. ADICIONE ESTES CAMPOS ---
    private Long tanqueId;
    private String tanqueNumero;

    // --- 3. ADICIONE ESTE CONSTRUTOR ---
    // (Facilita a conversão da Entidade para DTO)
    public BicoDTO(Bico bico) {
        this.id = bico.getId();
        this.numero = bico.getNumero();
        if (bico.getTanque() != null) {
            this.tanqueId = bico.getTanque().getId();
            this.tanqueNumero = bico.getTanque().getNumero();
        }
    }

    // (O construtor que você tinha antes, (id, numero),
    // será substituído por este ou pelo @AllArgsConstructor do Lombok)
    public BicoDTO(Long id, String numero) {
        this.id = id;
        this.numero = numero;
    }
}
