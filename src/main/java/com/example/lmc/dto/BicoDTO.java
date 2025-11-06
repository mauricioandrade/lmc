package com.example.lmc.dto;

import com.example.lmc.entity.Bico;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BicoDTO {
    private Long id;
    private String numero;
    private Long tanqueId;
    private String tanqueNumero;

    public BicoDTO(Bico bico) {
        this.id = bico.getId();
        this.numero = bico.getNumero();
        if (bico.getTanque() != null) {
            this.tanqueId = bico.getTanque().getId();
            this.tanqueNumero = bico.getTanque().getNumero();
        }
    }
}
